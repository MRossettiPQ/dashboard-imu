package com.rot.mdns.services

import com.rot.core.config.ApplicationConfig
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.event.Observes
import jakarta.inject.Singleton
import java.io.IOException
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener


@Singleton
class MdnsService {
    private lateinit var jmdns: JmDNS

    fun onStart(@Observes event: StartupEvent) {
        val name = ApplicationConfig.config.mdns().name()
        val protocol = ApplicationConfig.config.mdns().protocol()
        val port = ApplicationConfig.config.backend().port()
        try {
            // Pega o IP da máquina (use InetAddress.getLocalHost() ou uma interface específica)
            val addr = InetAddress.getLocalHost()

            Log.info("Zeroconf starting register service on $addr")
            jmdns = JmDNS.create(addr)

            val serviceInfo = ServiceInfo.create(
                "_http._tcp.local.",
                name,
                port,
                "path=/api/core/ping" // TXT record opcional
            )


            // Add a service listener
            jmdns.addServiceListener("_http._tcp.local.", SampleListener())
            jmdns.registerService(serviceInfo)
            Log.info("Zeroconf registered service: $protocol://$name.local:$port")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.error("Zeroconf error on registering service: $protocol://$name.local:$port")
        }
    }

    fun onStop(@Observes event: ShutdownEvent) {
        try {
            Log.info("Zeroconf shutting down")
            jmdns.unregisterAllServices()

            jmdns.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

class SampleListener : ServiceListener {
    override fun serviceAdded(event: ServiceEvent) {
        Log.warn("Service added: " + event.info)
    }

    override fun serviceRemoved(event: ServiceEvent) {
        Log.warn("Service removed: " + event.info)
    }

    override fun serviceResolved(event: ServiceEvent) {
        Log.info("Service resolved: " + event.info)
    }
}