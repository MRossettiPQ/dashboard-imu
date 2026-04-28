package com.rot.core.utils

import io.quarkus.logging.Log
import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.narayana.jta.TransactionRunnerOptions
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Status.STATUS_COMMITTED
import jakarta.transaction.Synchronization
import jakarta.transaction.TransactionSynchronizationRegistry

object TransactionUtils {
    fun runInNewTransaction(): TransactionRunnerOptions {
        return QuarkusTransaction.requiringNew()
    }

    fun runInExistingTransaction(): TransactionRunnerOptions {
        return QuarkusTransaction.joiningExisting()
    }
}

@ApplicationScoped
class SessionUtils(
    private val tsr: TransactionSynchronizationRegistry
) {
    fun afterCommit(action: () -> Unit) {
        tsr.registerInterposedSynchronization(object : Synchronization {
            override fun beforeCompletion() {
                Log.warn("Not yet implemented")
            }
            override fun afterCompletion(status: Int) {
                if (status == STATUS_COMMITTED) {
                    action()
                }
            }
        })
    }
}
