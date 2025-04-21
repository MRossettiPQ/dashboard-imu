package com.rot.network.services

import com.rot.network.models.NetworkConfiguration
import com.rot.serial.dtos.SerialDeviceInfoRequestDto
import com.rot.serial.dtos.SerialWifiListResponseDto
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.transaction.Transactional
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import javax.jmdns.JmDNS


@ApplicationScoped
class NetworkConfigurationService(
    private val jmdns: JmDNS
) {

    @Transactional
    fun onDeviceInfoRequestDto(@Observes serialDeviceInfoRequestDto: SerialWifiListResponseDto) {
        println("onDeviceInfoRequestDto: $serialDeviceInfoRequestDto")
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
//            println("InetAddress: ${info.inetAddresses.joinToString()}")
        }

        val ip = detect()
        val gateway = gateway()
//        println("localHost: $localHost - $ip")
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

}