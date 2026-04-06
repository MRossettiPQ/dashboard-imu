package com.rot.network.services

import com.rot.core.config.ApplicationConfig
import io.quarkus.arc.runtime.BeanContainer
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownContext
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import org.eclipse.microprofile.config.ConfigProvider
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.nio.file.Files
import java.nio.file.Paths
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
            // 1. Busca todas as placas válidas
            val addresses = getValidIpAddresses()

            Log.infof("Placas de rede encontradas: %s", addresses.joinToString { it.hostAddress })

            if (addresses.isEmpty()) {
                Log.warn("Nenhuma placa de rede válida encontrada para o mDNS.")
                return
            }

            // 2. Prepara os dados base
            val rawName = config.mdns().name()
            val safeName = toURLFriendly(rawName)

            val backendPort = config.backend().port()
            val mqttPort = config.mqtt().port()

            // 3. Cria os templates para HTTP (Backend) e MQTT
            val httpServiceType = "_http._tcp.local."
            val httpProps = mutableMapOf("URL" to "http://$safeName.local:$backendPort/")
            val httpServiceInfo = ServiceInfo.create(httpServiceType, safeName, backendPort, 0, 0, httpProps)

            val mqttServiceType = "_mqtt._tcp.local."
            val mqttProps = mutableMapOf("URL" to "mqtt://$safeName.local:$mqttPort/")
            val mqttServiceInfo = ServiceInfo.create(mqttServiceType, safeName, mqttPort, 0, 0, mqttProps)

            // 4. Registra o mDNS em CADA uma das placas válidas
            for (address in addresses) {
                try {
                    Log.infof("Iniciando JmDNS em %s", address.hostAddress)

                    // Cria a instância do JmDNS UMA VEZ para esta placa de rede
                    val jmdns = JmDNS.create(address, safeName)

                    // Registra o serviço Backend (HTTP)
                    jmdns.registerService(httpServiceInfo.clone())
                    Log.info("mDNS Backend registrado: $safeName.$httpServiceType na porta $backendPort")

                    // Registra o serviço MQTT
                    jmdns.registerService(mqttServiceInfo.clone())
                    Log.info("mDNS MQTT registrado: $safeName.$mqttServiceType na porta $mqttPort")

                    // Salva a instância para podermos fechar depois no onStop
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
    fun getValidIpAddresses(): List<InetAddress> {
        val validAddresses = mutableListOf<InetAddress>()
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()

        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()

            // Ignora interfaces que estão desligadas ou são de01 loopback
            if (!networkInterface.isUp || networkInterface.isLoopback) {
                continue
            }

            val displayName = networkInterface.displayName.lowercase(Locale.getDefault())

            val inetAddresses = networkInterface.inetAddresses
            while (inetAddresses.hasMoreElements()) {
                val inetAddress = inetAddresses.nextElement()
                Log.info("Nome da placa de rede: $displayName - ${inetAddress.hostAddress}")

                // Aceita qualquer IPv4 não-loopback que passou pelo filtro de nome
                if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress) {
                    Log.infof("Placa de rede selecionada para mDNS: %s | IP: %s", networkInterface.displayName, inetAddress.hostAddress)
                    validAddresses.add(inetAddress)
                }
            }
        }

        return validAddresses
    }

//    @Transactional
//    @Scheduled(every = "5m", delay = 20, delayUnit = TimeUnit.SECONDS)
//    fun checkConnection() {
//        val actualNetwork = NetworkConfiguration.createQuery()
//            .where(NetworkConfiguration.q.active.isTrue)
//            .fetchFirst()
//
//        val localHost = InetAddress.getLocalHost()
//
//        val serviceInfos = jmdns.list("_http._tcp.local.")
//        for (info in serviceInfos) {
//            println("InetAddress: ${info.inetAddresses.joinToString()}")
//        }
//
//        val ip = detect()
//        println("localHost: $localHost - $ip")
//    }

    fun gateway(): String? {
        return ""
    }

    fun detect(): String? {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (iface in interfaces) {
            if (!iface.isUp || iface.isLoopback || iface.isVirtual) continue
            for (addr in iface.inetAddresses) {
                if (addr is Inet4Address && !addr.isLoopbackAddress) {
                    return addr.hostAddress
                }
            }
        }
        return null
    }

//    fun initMdns(container: BeanContainer, config: MdnsRuntimeConfig, shutdownContext: ShutdownContext) {
//        try {
//            val producer: JmDNSProducer = container.beanInstance(JmDNSProducer::class.java)
//            val inetAddress = getIpAddress()
//            val appName = ConfigProvider.getConfig().getOptionalValue("quarkus.application.name", String::class.java)
//            val httpHost = ConfigProvider.getConfig().getOptionalValue("quarkus.http.host", String::class.java)
//            val protocol = ConfigProvider.getConfig().getOptionalValue("quarkus.http.protocol", String::class.java) ?: "http"
//            if (httpHost.orElse("localhost") != "0.0.0.0") {
//                Log.warnf("For mDNS to work properly, 'quarkus.http.host' must be set to '0.0.0.0' for the local domain URL to be accessible.")
//            }
//            val defaultName = appName.orElse(inetAddress.hostName)
//            val name = toURLFriendly(config.host().orElse(defaultName))
//            Log.infof("Registering mDNS service '%s'", name)
//            val jmdns = JmDNS.create(inetAddress, name)
//            val port = ConfigProvider.getConfig().getOptionalValue("quarkus.http.port", Int::class.java)
//            val quarkusPort = port.orElse(8080)
//            val url = "$protocol://$name.local:$quarkusPort/"
//
//            val properties: MutableMap<String, String?> = HashMap()
//            properties["URL"] = url
//            properties.putAll(config.props())
//
//            val serviceInfo = ServiceInfo.create("_http._tcp.local.", name, quarkusPort, config.weight(), config.priority(), properties)
//
//            jmdns.registerService(serviceInfo)
//            Log.infof("The application is available from: %s", url)
////            producer.initialize(jmdns, url)
////            shutdownContext.addShutdownTask { producer.close() }
//        } catch (e: IOException) {
//            throw RuntimeException(e)
//        }
//    }

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

    /**
     * If running in container attempt to get the host address else return localhost. Retrieves the IP address of the local
     * machine. It iterates through all available
     * network interfaces and checks their IP addresses. If a suitable non-loopback,
     * site-local address is found, it is returned as the IP address of the local machine.
     *
     * @return the [InetAddress] representing the IP address of the local machine.
     * @throws UnknownHostException if the local host name could not be resolved into an address.
     * @throws SocketException if an I/O error occurs when querying the network interfaces.
     */
    @Throws(UnknownHostException::class, SocketException::class)
    fun getIpAddress(): InetAddress {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        val fallbackAddress = InetAddress.getLocalHost()

        Log.infof("Buscando placa de rede física. Fallback (Padrão OS): %s", fallbackAddress.hostAddress)

        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()

            // Ignora interfaces que estão desligadas, loopback (127.0.0.1) ou virtuais
            if (!networkInterface.isUp || networkInterface.isLoopback || networkInterface.isVirtual) {
                continue
            }

            // (Opcional) Ignora nomes conhecidos de adaptadores de VPN no Windows
            val displayName = networkInterface.displayName.lowercase()
            if (displayName.contains("vpn") || displayName.contains("radmin") || displayName.contains("hamachi") || displayName.contains("zerotier")) {
                continue
            }

            val inetAddresses = networkInterface.inetAddresses
            while (inetAddresses.hasMoreElements()) {
                val inetAddress = inetAddresses.nextElement()

                // O segredo está aqui: Forçamos ser IPv4 e exigimos que seja SiteLocalAddress
                // (Isso garante que pegue ranges como 192.168.x.x ou 10.x.x.x e ignore o 26.x.x.x da VPN)
                if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress && inetAddress.isSiteLocalAddress) {
                    Log.infof("Placa de rede forçada: %s | IP: %s", networkInterface.displayName, inetAddress.hostAddress)
                    return inetAddress
                }
            }
        }

        Log.warnf("Não foi possível isolar a placa local. Usando fallback: %s", fallbackAddress.hostAddress)
        return fallbackAddress
    }

    /**
     * Determines if the application is running inside a container (such as Docker or Kubernetes).
     * This is done by inspecting the '/proc/1/cgroup' file and checking for the presence of
     * "docker" or "kubepods". Additionally, it checks specific environment variables to verify
     * the container environment.
     *
     * @return `true` if the application is running inside a container; `false` otherwise.
     */
    fun isRunningInContainer(): Boolean {
        try {
            val lines = Files.readAllLines(Paths.get("/proc/1/cgroup"))
            for (line in lines) {
                if (line.contains("docker") || line.contains("kubepods")) {
                    return true
                }
            }
        } catch (e: IOException) {
            // Ignore, likely not in a container if the file doesn't exist
        }

        // check environment variables
        return System.getenv("CONTAINER") != null || System.getenv("KUBERNETES_SERVICE_HOST") != null
    }

}