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

    interface FrontendConfig {
        @WithName("url")
        fun url(): String
    }

    interface SecurityConfig {
        @WithName("token")
        fun token(): String
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