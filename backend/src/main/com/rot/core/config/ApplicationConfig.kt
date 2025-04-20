package com.rot.core.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.SmallRyeConfig
import io.smallrye.config.WithName
import org.eclipse.microprofile.config.ConfigProvider

@ConfigMapping(
    prefix = "application",
    namingStrategy = ConfigMapping.NamingStrategy.KEBAB_CASE
)
interface ApplicationConfig {

    companion object {
        val config: ApplicationConfig = ConfigProvider.getConfig()
            .unwrap(SmallRyeConfig::class.java)
            .getConfigMapping(ApplicationConfig::class.java)
    }

    @WithName("environment")
    fun environment(): String

    @WithName("name")
    fun name(): String

    @WithName("security")
    fun security(): SecurityConfig

    fun isDev(): Boolean {
        return environment() == "DEVELOPMENT"
    }

    @WithName("database")
    fun database(): DatabaseConfig

    @WithName("frontend")
    fun frontend(): FrontendConfig

    @WithName("backend")
    fun backend(): BackendConfig

    @WithName("mqtt")
    fun mqtt(): MqttConfig

    @WithName("socket")
    fun socket(): SocketConfig

    @WithName("mdns")
    fun mdns(): MdnsConfig

    interface MdnsConfig {
        @WithName("name")
        fun name(): String

        @WithName("protocol")
        fun protocol(): String
    }

    interface MqttConfig {
        @WithName("port")
        fun port(): Int

        @WithName("host")
        fun host(): String
    }

    interface SocketConfig {
        @WithName("port")
        fun port(): Int

        @WithName("host")
        fun host(): String
    }

    interface BackendConfig {
        @WithName("port")
        fun port(): Int

        @WithName("protocol")
        fun protocol(): String

        @WithName("host")
        fun host(): String
    }

    interface FrontendConfig {
        @WithName("url")
        fun url(): String
    }

    interface SecurityConfig {
        @WithName("token")
        fun token(): String

        @WithName("issuer")
        fun issuer(): String

        @WithName("subject")
        fun subject(): String
    }

    interface DatabaseConfig {
        @WithName("host")
        fun host(): String

        @WithName("name")
        fun name(): String

        @WithName("username")
        fun username(): String

        @WithName("password")
        fun password(): String
    }
}