package com.rot.serial.services

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import com.rot.core.config.ApplicationConfig
import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import java.util.concurrent.TimeUnit


@ApplicationScoped
class SerialService {
    private var serialPort: SerialPort? = null

    @Scheduled(every = "15s", delay = 20, delayUnit = TimeUnit.SECONDS)
    fun checkConnection() {

    }

    fun findPortByProductName(targetProduct: String): SerialPort? {
        val ports = SerialPort.getCommPorts()
        for (port in ports) {
            val descriptor = port.descriptivePortName?.lowercase() ?: ""
            val productName = port.portDescription?.lowercase() ?: ""

            if (descriptor.contains(targetProduct.lowercase()) || productName.contains(targetProduct.lowercase())) {
                println("🔌 Encontrado: ${port.systemPortName} - ${port.descriptivePortName}")
                return port
            }
        }
        Log.warn("❌ Nenhum dispositivo encontrado com o nome: $targetProduct")
        return null
    }

    fun init() {
        serialPort = findPortByProductName(ApplicationConfig.config.serial().name())
        serialPort?.let { serial ->
            serial.baudRate = ApplicationConfig.config.serial().baudRate()
            serial.openPort()

            serial.addDataListener(object : SerialPortDataListener {
                override fun getListeningEvents(): Int {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE
                }

                override fun serialEvent(event: SerialPortEvent) {
                    if (event.eventType != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return
                    val received = readData(serialPort!!.bytesAvailable())

                    Log.info("🔌 Dados recebidos: $received")
                    // Aqui você pode processar os dados, salvar no banco, enviar para frontend etc.
                }
            })
        }
    }

    fun sendData(data: String) {
        serialPort?.let { serial ->
            if (serial.isOpen) {
                serial.writeBytes(data.toByteArray(Charsets.UTF_8), data.length)
            }
        }
    }

    fun readData(size: Int): String {
        val buffer = ByteArray(size)
        val numRead = serialPort!!.readBytes(buffer, buffer.size)
        return String(buffer, 0, numRead)
    }


}