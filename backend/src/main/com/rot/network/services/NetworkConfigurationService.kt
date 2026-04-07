package com.rot.network.services

import com.rot.core.config.ApplicationConfig
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.rmi.UnknownHostException
import java.util.*
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo


@ApplicationScoped
class NetworkConfigurationService(
    private val config: ApplicationConfig
) {

    private val jmdnsInstances = mutableListOf<JmDNS>()

    fun onStart(@Observes ev: StartupEvent) {
        try {
            val addresses = getValidIpAddresses()

            if (addresses.isEmpty()) {
                Log.warn("Nenhuma placa de rede válida encontrada para o mDNS.")
                return
            }

            val rawName = config.mdns().name()
            val safeName = toURLFriendly(rawName)

            val backendPort = config.backend().port()
            val mqttPort = config.mqtt().port()
            val mqttWsPort = config.mqtt().socketPort() // Obtendo a porta WebSocket

            val httpServiceType = "_http._tcp.local."
            val httpProps = mutableMapOf("URL" to "http://$safeName.local:$backendPort/")
            val httpServiceInfo = ServiceInfo.create(httpServiceType, safeName, backendPort, 0, 0, httpProps)
            val httpUrl = httpProps["URL"]

            val mqttServiceType = "_mqtt._tcp.local."
            val mqttProps = mutableMapOf("URL" to "mqtt://$safeName.local:$mqttPort/")
            val mqttServiceInfo = ServiceInfo.create(mqttServiceType, safeName, mqttPort, 0, 0, mqttProps)
            val mqttUrl = mqttProps["URL"]

            val mqttWsServiceType = "_ws._tcp.local."
            val mqttWsProps = mutableMapOf("URL" to "ws://$safeName.local:$mqttWsPort/")
            val mqttWsServiceInfo = ServiceInfo.create(mqttWsServiceType, safeName, mqttWsPort, 0, 0, mqttWsProps)
            val mqttWsUrl = mqttWsProps["URL"]

            for ((networkInterface, address) in addresses) {
                try {
                    val jmdns = JmDNS.create(address, safeName)
                    val displayName = networkInterface.displayName.lowercase(Locale.getDefault())

                    // Registra os serviços Backend (HTTP), MQTT (TCP) e MQTT (WebSocket)
                    jmdns.registerService(httpServiceInfo.clone())
                    jmdns.registerService(mqttServiceInfo.clone())
                    jmdns.registerService(mqttWsServiceInfo.clone())

                    Log.info("Placa de rede: $displayName - ${address.hostAddress} - mDNS MQTT: $mqttUrl, MQTT-WS: $mqttWsUrl e Backend: $httpUrl")
                    jmdnsInstances.add(jmdns)
                } catch (e: Exception) {
                    Log.error("Failed to register mDNS services on ${address.hostAddress}", e)
                }
            }
        } catch (e: Exception) {
            Log.error("Failed to setup mDNS services", e)
        }
    }

    fun onStop(@Observes ev: ShutdownEvent) {
        // Desregistra e fecha todas as instâncias quando o Quarkus desligar
        for (jmdns in jmdnsInstances) {
            try {
                jmdns.unregisterAllServices()
                jmdns.close()
            } catch (e: Exception) {
                Log.warn("Erro ao fechar instância JmDNS", e)
            }
        }
        jmdnsInstances.clear()
        Log.info("mDNS services unregistered from all interfaces")
    }

    @Throws(UnknownHostException::class, SocketException::class)
    fun getValidIpAddresses(): List<Pair<NetworkInterface, InetAddress>> {
        val validAddresses = mutableListOf<Pair<NetworkInterface, InetAddress>>()
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()

        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()

            // Ignora interfaces que estão desligadas ou são de01 loopback
            if (!networkInterface.isUp || networkInterface.isLoopback) {
                continue
            }

            val inetAddresses = networkInterface.inetAddresses
            while (inetAddresses.hasMoreElements()) {
                val inetAddress = inetAddresses.nextElement()
                // Aceita qualquer IPv4 não-loopback que passou pelo filtro de nome
                if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress) {
//                    Log.infof("Placa de rede selecionada para mDNS: %s | IP: %s", networkInterface.displayName, inetAddress.hostAddress)
                    validAddresses.add(Pair(networkInterface, inetAddress))
                }
            }
        }

        return validAddresses
    }

    private fun toURLFriendly(input: String?): String {
        if (input.isNullOrEmpty()) {
            return ""
        }

        // Trim the input string and replace multiple spaces with a single space
        val cleanedInput = input.trim { it <= ' ' }.replace("\\s+".toRegex(), " ")

        // Replace spaces with hyphens and convert to lowercase
        var urlFriendly = cleanedInput.replace(" ", "-").lowercase(Locale.getDefault())

        // Optionally remove other characters that might not be URL friendly
        urlFriendly = urlFriendly.replace("[^a-z0-9\\-]".toRegex(), "")

        return urlFriendly
    }

}