package com.rot.core.hibernate.implementation

import org.hibernate.boot.model.naming.*
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment

class ImplicitNamingStrategyImpl : ImplicitNamingStrategyJpaCompliantImpl() {
    override fun determineForeignKeyName(source: ImplicitForeignKeyNameSource): Identifier {
        return getConstraintName("fk", source)
    }

    override fun determineUniqueKeyName(source: ImplicitUniqueKeyNameSource): Identifier {
        return getConstraintName("uk", source)
    }

    private fun getConstraintName(prefix: String, source: ImplicitConstraintNameSource): Identifier {
        val table = source.tableName?.text
            ?.replace("vw", "")
            ?.replace("_", "")

        val columnName = source.columnNames
            .firstOrNull()
            ?.text
            ?.replace("_id", "")
            ?.replace("_", "")

        var name = "${prefix}_${table}_${columnName}".lowercase()

        // ⚠️ Postgres aceita no máximo 63 chars
        if (name.length > 63) {
            name = name.substring(0, 63)
        }

        val jdbcEnv = source.buildingContext
            .buildingOptions
            .serviceRegistry
            .getService(JdbcEnvironment::class.java)
        return jdbcEnv!!.identifierHelper.toIdentifier(name)
    }
}
