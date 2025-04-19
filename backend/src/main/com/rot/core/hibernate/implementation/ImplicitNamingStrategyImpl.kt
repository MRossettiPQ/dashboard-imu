package com.rot.core.hibernate.implementation

import org.hibernate.boot.model.naming.*

class ImplicitNamingStrategyImpl : ImplicitNamingStrategyJpaCompliantImpl() {

    override fun determineForeignKeyName(source: ImplicitForeignKeyNameSource): Identifier {
        return getConstraintName("fk", source)
    }

    private fun getConstraintName(prefix: String, source: ImplicitConstraintNameSource): Identifier {
        val table = source.tableName?.text?.replace("vw", "")?.replace("_", "")
        val columnName = source.columnNames?.get(0)?.text?.replace("_id", "")?.replace("_", "")
        val name = "${prefix}_${table}_$columnName".lowercase()
        return toIdentifier(name, source.buildingContext)
    }

    override fun determineUniqueKeyName(source: ImplicitUniqueKeyNameSource): Identifier {
        return getConstraintName("uk", source)
    }

}