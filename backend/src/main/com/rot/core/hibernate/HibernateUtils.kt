package com.rot.core.hibernate

import jakarta.persistence.Id
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

inline fun <reified T : Any> findIdField(): String? {
    val kClass = T::class
    for (prop in kClass.declaredMemberProperties) {
        val field = prop.javaField ?: continue
        if (field.isAnnotationPresent(Id::class.java)) {
            return prop.name
        }
    }
    return null
}

fun getIdValue(instance: Any): Any? {
    val clazz = instance::class
    for (prop in clazz.declaredMemberProperties) {
        val field = prop.javaField ?: continue
        if (field.isAnnotationPresent(Id::class.java)) {
            prop.isAccessible = true
            return prop.getter.call(instance)
        }
    }
    return null
}
