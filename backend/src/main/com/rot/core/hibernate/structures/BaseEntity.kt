package com.rot.core.hibernate.structures

import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.jpa.impl.JPAQuery
import com.rot.core.context.ApplicationContext
import io.quarkus.hibernate.orm.panache.PanacheEntityBase.getEntityManager
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*

fun <T : Any> getEntityPathBase(entityClass: Class<T>): EntityPathBase<T>? {
    return try {
        // Tenta descobrir a classe do tipo Q gerada pelo QueryDSL, ex: "com.example.QUser"
        val qClassName = entityClass.name.replaceAfterLast('.', "Q${entityClass.simpleName}")
        val qClass = Class.forName(qClassName)

        // Pega o campo estático, ex: `public static final QUser user`
        val fieldName = entityClass.simpleName.replaceFirstChar(Char::lowercase)
        val field = qClass.getField(fieldName)

        @Suppress("UNCHECKED_CAST")
        field[null] as? EntityPathBase<T>
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

interface BaseCompanion<T : PanacheEntityBase, Id : Any>: PanacheCompanionBase<T, Id> {
    val entityClass: Class<T>

    val q: EntityPathBase<T>?
        get() = getEntityPathBase(entityClass)

    fun createQuery(em: EntityManager = getEntityManager()): JPAQuery<T> {
        return JPAQuery<T>(em).from(q).select(q)
    }
}


@MappedSuperclass
abstract class BaseEntity<T : PanacheEntityBase> : PanacheEntityBase, Serializable {

    private val simpleName: String
        get() = javaClass.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    var createdAt: LocalDateTime? = null

    @Column(name = "created_by")
    var createdBy: String? = null

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    var updatedAt: LocalDateTime? = null

    @Column(name = "updated_by")
    var updatedBy: String? = null

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null

    abstract val id: Any?

    @PrePersist
    fun preCreate() {
        audit()
    }

    @PreUpdate
    fun preUpdate() {
        audit()
    }

    private fun audit() {
        val context = ApplicationContext.context
        if (isNewBean) {
            createdAt = LocalDateTime.now()
        }

        if (isNewBean && context != null) {
            createdBy = context.id.toString()
        }

        if (context != null) {
            updatedBy = context.id.toString()
        }

        updatedAt = LocalDateTime.now()
    }

    @Transient
    val extraData: MutableMap<String, Any?> = HashMap()

    @get:Transient
    val isNewBean: Boolean
        get() = Objects.isNull(id)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is BaseEntity<*>) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }

    override fun toString(): String {
        return javaClass.simpleName + "[id: $id]"
    }

    fun validate() {
        val errors = validator.validate(this)
        if (errors.isNotEmpty()) {
            throw ConstraintViolationException(errors)
        }
    }

    fun save(): BaseEntity<T>? {
        if (!isNewBean) return getEntityManager().merge(this)
        getEntityManager().persist(this)
        return this
    }


}

private val validator = Validation.buildDefaultValidatorFactory().validator