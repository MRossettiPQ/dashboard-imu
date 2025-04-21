package com.rot.serial.services

import com.fasterxml.jackson.databind.JsonNode
import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import com.rot.core.config.ApplicationConfig
import com.rot.core.utils.JsonUtils
import com.rot.serial.dtos.*
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Event
import jakarta.enterprise.event.Observes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@ApplicationScoped
class SerialService(
    var emitterDeviceInfoRequestDto: Event<SerialDeviceInfoRequestDto>,
    var emitterWifiListRequestDto: Event<SerialWifiListRequestDto>,
    var emitterWifiListResponseDto: Event<SerialWifiListResponseDto>
) : AbstractSerialPortData() {
    private var serialPort: SerialPort? = null

    @Scheduled(
        every = "25s",
        delay = 20,
        delayUnit = TimeUnit.SECONDS,
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    suspend fun checkConnection() {
        if (serialPort?.isOpen != true) connectEspBroker()
    }

    fun onStop(@Observes ev: ShutdownEvent?) {
        if (serialPort?.isOpen == true) runCatching {
            serialPort?.closePort()
        }
    }

    private suspend fun tryToConnect(serial: SerialPort): SerialPort? {
        var returnSerial: SerialPort? = null
        abstractListenerAndStart(
            serial = serial,
            onData = { _: SerialPortEvent, port: SerialPort, received: String, node: JsonNode ->
                when (SerialMessageType.valueOf(node["type"].toString())) {
                    SerialMessageType.DEVICE_INFO_RESPONSE -> {
                        val converted = JsonUtils.MAPPER.readValue(received, SerialDeviceInfoResponseDto::class.java)
                        if (converted.data?.productName?.contains(ApplicationConfig.config.serial().name()) == true) {
                            Log.info("ESP32 device detected on port ${serial.systemPortName}")
                            byDescriptor = true
                            returnSerial = serial
                        }
                    }

                    else -> Log.info("Message router for message ${port.systemPortName} not found")
                }
            },
            onStarted = {
                Log.info("Connected to port: ${serial.systemPortName}, waiting for JSON data")
                try {
                    // Use withContext to run the blocking read operation in a background thread
                    returnSerial = withContext(Dispatchers.IO) {
                        val startTime = System.currentTimeMillis()
                        while (System.currentTimeMillis() - startTime < 4000 && returnSerial == null) {
                            delay(200)
                        }
                        serial.closePort()
                        return@withContext returnSerial
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    serial.closePort()
                    returnSerial?.closePort()
                }
            }
        )
        return returnSerial
    }

    private suspend fun findPortByManufacturer(manufacturer: String): SerialPort? {
        val ports = SerialPort.getCommPorts()
        Log.warn("Fallback with findByManufacturer: $manufacturer - ${ports.size}")
        for (port in ports) {
            if (port.manufacturer.contains(manufacturer)) {
                Log.info("Trying to connect to port: ${port.systemPortName}")
                val connectedPort = tryToConnect(port)
                if (connectedPort != null) return connectedPort
            }
        }
        Log.warn("No device found with the manufacturer: $manufacturer")
        return null
    }

    suspend fun connectEspBroker() {
        Log.info("Find serial port for ESP32 broker")
        serialPort = findPortByProductName(ApplicationConfig.config.serial().name())
            ?: findPortByManufacturer(ApplicationConfig.config.serial().manufacturer())

        serialPort?.let {
            Log.info("Achei essa porta, tentar conectar ${it.systemPortPath}")
            abstractListenerAndStart(
                serial = it,
                onData = { _: SerialPortEvent, port: SerialPort, received: String, node: JsonNode ->
                    when (SerialMessageType.valueOf(node["type"].toString())) {
                        SerialMessageType.DEVICE_INFO_RESPONSE -> {
                            val data = JsonUtils.MAPPER.convertValue(received, SerialDeviceInfoResponseDto::class.java)
                            sendData(port, SerialWifiListRequestDto())
                        }

                        SerialMessageType.WIFI_LIST_RESPONSE -> {
                            val data = JsonUtils.MAPPER.convertValue(received, SerialWifiListResponseDto::class.java)
                            emitterWifiListResponseDto.fireAsync(data)
                        }

                        else -> Log.info("Message router for message ${port.systemPortName} not found")
                    }
                }
            )
        }
    }

    fun onDeviceInfoRequestDto(@Observes serialDeviceInfoRequestDto: SerialDeviceInfoRequestDto) {
        sendData(serialPort, serialDeviceInfoRequestDto)
    }

    fun onWifiListRequestDto(@Observes serialWifiListRequestDto: SerialWifiListRequestDto) {
        sendData(serialPort, serialWifiListRequestDto)
    }

    fun onWifiListResponseDto(@Observes serialWifiListResponseDto: SerialWifiListResponseDto) {
        // TODO save wifi list
    }

    fun onWifiRegisterRequestDto(@Observes serialWifiListResponseDto: SerialWifiRegisterRequestDto) {
        sendData(serialPort, serialWifiListResponseDto)
    }
}

abstract class AbstractSerialPortData {
    var byDescriptor: Boolean = false

    fun findPortByProductName(targetProduct: String): SerialPort? {
        val ports = SerialPort.getCommPorts()
        Log.warn("Starting serial by product name: $targetProduct - ${ports.size}")
        for (port in ports) {
            val descriptor = port.descriptivePortName?.lowercase() ?: ""
            val productName = port.portDescription?.lowercase() ?: ""
            if (descriptor.contains(targetProduct.lowercase()) || productName.contains(targetProduct.lowercase())) {
                Log.info("Found: ${port.systemPortName} - ${port.descriptivePortName}")
                byDescriptor = false
                return port
            }
        }
        Log.warn("No device found with the name: $targetProduct")
        return null
    }

    private fun openPort(port: SerialPort): Boolean {
        try {
            port.baudRate = ApplicationConfig.config.serial().baudRate()
            return port.openPort()
        } catch (e: Exception) {
            port.closePort()
            Log.error("Error opening port: ${port.systemPortName}", e)
            return false
        }
    }

    suspend fun abstractListenerAndStart(serial: SerialPort, onData: (event: SerialPortEvent, port: SerialPort, received: String, node: JsonNode) -> Unit, onStarted: (suspend () -> Unit?)? = null) {
        serial.addDataListener(object : SerialPortDataListener {
            override fun getListeningEvents(): Int {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED
            }

            override fun serialEvent(event: SerialPortEvent) {
                if (event.eventType != SerialPort.LISTENING_EVENT_DATA_RECEIVED) return
                val available = serial.bytesAvailable()
                if (available <= 0) return

                val received = readData(serial, available)
                Log.info("Data received: $received")
                if (received.startsWith("{") && received.endsWith("\n") && JsonUtils.isValidJson(received)) runCatching {
                    val message = JsonUtils.toJsonNode(received)
                    onData(event, serial, received, message)
                }
            }
        })

        if (openPort(serial)) {
            onStarted?.invoke()
            Log.info("Port ${serial.systemPortName} opened successfully.")
        }
    }

    fun <T> sendData(serial: SerialPort? = null, data: T) {
        if (serial == null) return
        val json = JsonUtils.MAPPER.writeValueAsString(data)
        sendDataSerialized(serial, json)
    }

    private fun sendDataSerialized(serial: SerialPort, data: String) {
        try {
            if (serial.isOpen) serial.writeBytes("${data}\n".toByteArray(Charsets.UTF_8), data.length)
        } catch (e: Exception) {
            Log.error("Error on serialize and send data: ${e.message}")
        }
    }

    private fun readData(serial: SerialPort, size: Int): String {
        if (size <= 0) return ""
        val buffer = ByteArray(size)
        val numRead = serial.readBytes(buffer, buffer.size)
        return if (numRead > 0) String(buffer, 0, numRead) else ""
    }
}