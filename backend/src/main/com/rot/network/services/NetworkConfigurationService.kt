package com.rot.network.services

import com.rot.network.models.NetworkConfiguration
import io.quarkiverse.mdns.runtime.JmDNSProducer
import io.quarkiverse.mdns.runtime.MdnsRuntimeConfig
import io.quarkus.arc.runtime.BeanContainer
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownContext
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.transaction.Transactional
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
import java.util.concurrent.TimeUnit
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo


@ApplicationScoped
class NetworkConfigurationService(
    private val jmdns: JmDNS
) {

    fun onStart(@Observes ev: StartupEvent?) {
        Log.info("The application is starting...")
    }

    fun onStop(@Observes ev: ShutdownEvent?) {
        Log.info("The application is stopping...")
    }

    @Transactional
    @Scheduled(every = "5m", delay = 20, delayUnit = TimeUnit.SECONDS)
    fun checkConnection() {
        val actualNetwork = NetworkConfiguration.createQuery()
            .where(NetworkConfiguration.q.active.isTrue)
            .fetchFirst()

        val localHost = InetAddress.getLocalHost()

        val serviceInfos = jmdns.list("_http._tcp.local.")
        for (info in serviceInfos) {
            println("InetAddress: ${info.inetAddresses.joinToString()}")
        }

        val ip = detect()
        println("localHost: $localHost - $ip")
    }

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

    fun initMdns(container: BeanContainer, config: MdnsRuntimeConfig, shutdownContext: ShutdownContext) {
        try {
            val producer: JmDNSProducer = container.beanInstance(JmDNSProducer::class.java)
            val inetAddress = getIpAddress()
            val appName = ConfigProvider.getConfig().getOptionalValue("quarkus.application.name", String::class.java)
            val httpHost = ConfigProvider.getConfig().getOptionalValue("quarkus.http.host", String::class.java)
            val protocol = ConfigProvider.getConfig().getOptionalValue("quarkus.http.protocol", String::class.java) ?: "http"
            if (httpHost.orElse("localhost") != "0.0.0.0") {
                Log.warnf("For mDNS to work properly, 'quarkus.http.host' must be set to '0.0.0.0' for the local domain URL to be accessible.")
            }
            val defaultName = appName.orElse(inetAddress.hostName)
            val name = toURLFriendly(config.host().orElse(defaultName))
            Log.infof("Registering mDNS service '%s'", name)
            val jmdns = JmDNS.create(inetAddress, name)
            val port = ConfigProvider.getConfig().getOptionalValue("quarkus.http.port", Int::class.java)
            val quarkusPort = port.orElse(8080)
            val url = "$protocol://$name.local:$quarkusPort/"

            val properties: MutableMap<String, String?> = HashMap()
            properties["URL"] = url
            properties.putAll(config.props())

            val serviceInfo = ServiceInfo.create("_http._tcp.local.", name, quarkusPort, config.weight(), config.priority(), properties)

            jmdns.registerService(serviceInfo)
            Log.infof("The application is available from: %s", url)
//            producer.initialize(jmdns, url)
//            shutdownContext.addShutdownTask { producer.close() }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
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
        // Get all network interfaces
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()

        var localhost = InetAddress.getLocalHost()
        // Print out information about each IP address
        Log.infof("Localhost %s IP Address: %s", localhost.hostName, localhost.hostAddress)

        if (!isRunningInContainer()) {
            return localhost
        }

        // Iterate through all interfaces
        while (networkInterfaces.hasMoreElements()) {
            val networkInterface = networkInterfaces.nextElement()

            // Get all IP addresses for each network interface
            val inetAddresses = networkInterface.inetAddresses

            while (inetAddresses.hasMoreElements()) {
                val inetAddress = inetAddresses.nextElement()

                if (!inetAddress.isLoopbackAddress && inetAddress.isSiteLocalAddress) {
                    // Print out information about each IP address
                    val displayName = networkInterface.displayName
                    Log.infof("Network Interface: %s", displayName)
                    Log.infof("%s IP Address: %s", inetAddress.hostName, inetAddress.hostAddress)
                    Log.debugf("    Loopback: %s", inetAddress.isLoopbackAddress)
                    Log.debugf("    Site Local: %s", inetAddress.isSiteLocalAddress)
                    Log.debugf("    Multicast: %s", inetAddress.isMulticastAddress)
                    Log.debugf("    Any Local: %s", inetAddress.isAnyLocalAddress)
                    Log.debugf("    Link Local: %s", inetAddress.isLinkLocalAddress)

                    if (displayName.startsWith("wlan") || displayName.startsWith("eth")) {
                        Log.infof("Running in Docker: %s %s %s", displayName, inetAddress.hostName, inetAddress.hostAddress)
                        localhost = inetAddress
                    }
                }
            }
        }
        Log.infof("Container %s IP Address: %s", localhost.hostName, localhost.hostAddress)
        return localhost
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