package com.rot

import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

@ApplicationScoped
class ApplicationLifecycle {

    fun onStart(@Observes ev: StartupEvent?) {
        Log.info("The application is starting...")
        Log.info("Database migrations applied successfully.")
    }

    fun onStop(@Observes ev: ShutdownEvent?) {
        Log.info("The application is stopping...")
    }

}