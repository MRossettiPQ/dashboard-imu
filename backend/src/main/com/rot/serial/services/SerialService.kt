package com.rot.serial.services

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import com.rot.core.config.ApplicationConfig
import com.rot.core.utils.JsonUtils
import com.rot.serial.dtos.*
import io.quarkus.logging.Log
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
) {
    private var byDescriptor: Boolean = false
    private var serialPort: SerialPort? = null

    @Scheduled(
        every = "25s",
        delay = 20,
        delayUnit = TimeUnit.SECONDS,
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    suspend fun checkConnection() {
        if (serialPort?.isOpen != true) init()
    }

    private fun openPort(port: SerialPort): Boolean {
        try {
            port.baudRate = ApplicationConfig.config.serial().baudRate()
            return port.openPort()
        } catch (e: Exception) {
            Log.error("Error opening port: ${port.systemPortName}", e)
            return false
        }
    }

    private suspend fun readAndParseData(port: SerialPort): SerialMessageDto<*>? {
        val buffer = ByteArray(1024)
        val startTime = System.currentTimeMillis()
        // Use withContext to run the blocking read operation in a background thread
        return withContext(Dispatchers.IO) {
            while (System.currentTimeMillis() - startTime < 5000) {
                if (port.bytesAvailable() > 0) {
                    val received = readData(port, buffer.size)
                    if (received.startsWith("{")) {
                        val data = JsonUtils.toObject(received, SerialMessageDto::class.java)
                        Log.info("📥 Data received: $received")
                        return@withContext data
                    } else {
                        Log.warn("📥 Invalid data received (missing '{'): $received")
                    }
                }

                // Use delay instead of Thread.sleep to avoid blocking the thread
                delay(200)  // Non-blocking sleep
            }
            port.closePort()
            return@withContext null
        }
    }

    private fun handleDeviceData(port: SerialPort, data: SerialMessageDto<*>): SerialPort? {
        return when (data.type) {
            SerialMessageType.DEVICE_INFO_RESPONSE -> {
                if (data is SerialDeviceInfoResponseDto && data.data?.productName?.contains(ApplicationConfig.config.serial().name()) == true) {
                    Log.info("✅ ESP32 device detected on port ${port.systemPortName}")
                    byDescriptor = true
                    port
                } else {
                    null
                }
            }
            else -> null
        }
    }

    private suspend fun tryToConnect(port: SerialPort): SerialPort? {
        if (openPort(port)) {
            Log.info("Connected to port: ${port.systemPortName}, waiting for JSON data")
            val data = readAndParseData(port)
            if (data != null) {
                return handleDeviceData(port, data)
            }
        }
        return null
    }

    private suspend fun findPortByManufacturer(manufacturer: String): SerialPort? {
        Log.warn("Fallback with findByManufacturer: $manufacturer")
        val ports = SerialPort.getCommPorts()
        for (port in ports) {
            if (port.manufacturer.contains(manufacturer)) {
                Log.info("Trying to connect to port: ${port.systemPortName}")
                val connectedPort = tryToConnect(port)
                if (connectedPort != null) return connectedPort
            }
        }
        return null
    }

    private fun findPortByProductName(targetProduct: String): SerialPort? {
        Log.warn("Starting serial by product name: $targetProduct")
        val ports = SerialPort.getCommPorts()
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

    suspend fun init() {
        Log.info("Find serial port for ESP32 broker")
        serialPort = findPortByProductName(ApplicationConfig.config.serial().name())
            ?: findPortByManufacturer(ApplicationConfig.config.serial().manufacturer())

        serialPort?.let { serial ->
            Log.info("Achei essa porta, tentar conectar ${serial.systemPortPath}")
            if (openPort(serial)) {
                serial.addDataListener(object : SerialPortDataListener {
                    override fun getListeningEvents(): Int {
                        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE
                    }

                    override fun serialEvent(event: SerialPortEvent) {
                        if (event.eventType != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return
                        val received = readData(serial, serial.bytesAvailable())

                        Log.info("Data received: $received")
                        val message = JsonUtils.toObject(received, SerialMessageDto::class.java)
                        messageRouter(serial, message)
                    }
                })
                Log.info("Port ${serial.systemPortName} opened successfully.")
            }
        }
    }

    fun messageRouter(port: SerialPort, message: SerialMessageDto<*>) {
        when (message.type) {
            SerialMessageType.DEVICE_INFO_RESPONSE -> {
                val data = JsonUtils.MAPPER.convertValue(message, SerialDeviceInfoResponseDto::class.java)
                println(data)
            }

            SerialMessageType.WIFI_LIST_RESPONSE -> {
                val data = JsonUtils.MAPPER.convertValue(message, SerialWifiListResponseDto::class.java)
                emitterWifiListResponseDto.fireAsync(data)
            }

            else -> Log.info("Message router for message ${port.systemPortName} not found")
        }
    }

    fun onDeviceInfoRequestDto(@Observes serialDeviceInfoRequestDto: SerialDeviceInfoRequestDto) {
        sendData(serialPort, serialDeviceInfoRequestDto)
    }

    fun onWifiListResponseDto(@Observes serialWifiListResponseDto: SerialWifiListResponseDto) {
        sendData(serialPort, serialWifiListResponseDto)
    }

    fun onWifiRegisterRequestDto(@Observes serialWifiListResponseDto: SerialWifiRegisterRequestDto) {
        sendData(serialPort, serialWifiListResponseDto)
    }

    fun <T> sendData(serial: SerialPort? = null, data: T) {
        println("sending data: $data")
        if (serial == null) {
            return
        }
        val json = JsonUtils.MAPPER.writeValueAsString(data)
        sendData(serial, json)
    }

    fun sendData(serial: SerialPort, data: String) {
        try {
            if (serial.isOpen) serial.writeBytes(data.toByteArray(Charsets.UTF_8), data.length)
        } catch (e: Exception) {
            Log.error("Erro ao serializar e enviar dados: ${e.message}")
        }
    }

    fun readData(serial: SerialPort, size: Int): String {
        val buffer = ByteArray(size)
        val numRead = serial.readBytes(buffer, buffer.size)
        return String(buffer, 0, numRead)
    }
}
