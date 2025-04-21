package com.rot.core.utils

import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.narayana.jta.TransactionRunnerOptions

object TransactionUtils {
    fun runInNewTransaction(): TransactionRunnerOptions {
        return QuarkusTransaction.requiringNew()
    }

    fun runInExistingTransaction(): TransactionRunnerOptions {
        return QuarkusTransaction.joiningExisting()
    }
}