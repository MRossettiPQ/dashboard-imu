package com.rot

import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import org.flywaydb.core.Flyway

@ApplicationScoped
class ApplicationLifecycle(
    private val flyway: Flyway,
) {
    fun onStart(@Observes ev: StartupEvent?) {
        Log.info("The application is starting...")
        flyway.migrate()
    }

    fun onStop(@Observes ev: ShutdownEvent?) {
        Log.info("The application is stopping...")
    }
}