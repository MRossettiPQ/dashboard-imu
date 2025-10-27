package com.rot.core.hibernate.structures

import com.querydsl.core.types.EntityPath
import com.querydsl.jpa.JPQLQuery
import com.querydsl.jpa.impl.JPAQuery
import com.rot.core.context.ApplicationContext
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.findIdField
import com.rot.core.jaxrs.PaginationDto
import com.rot.core.utils.JsonUtils
import io.quarkus.hibernate.orm.panache.Panache
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation
import jakarta.ws.rs.core.Response
import java.io.Serializable
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.math.max
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

interface BaseCompanion<T : PanacheEntityBase, Id : Any, Q : EntityPath<T>> : PanacheCompanionBase<T, Id> {
    val entityClass: Class<T>
    val q: Q

    fun createQuery(em: EntityManager = Panache.getEntityManager()): JPAQuery<T> {
        return JPAQuery<T>(em).from(q).select(q)
    }

    fun findOrThrowById(id: Id, message: String? = "${entityClass.simpleName} not found"): T {
        return findOrThrowById(id, LockModeType.NONE, message)
    }

    fun findOrThrowById(id: Id, lockModeType: LockModeType? = LockModeType.NONE, message: String? = "${entityClass.simpleName} not found"): T {
        return findById(id, lockModeType!!)
            ?: throw ApplicationException(message!!, Response.Status.NOT_FOUND)
    }

    fun findOrCreateInstance(dtoIdValue: Any?): T {
        if (dtoIdValue == null) return entityClass.getDeclaredConstructor().newInstance()
        return (try {
            Panache.getEntityManager().find(entityClass, dtoIdValue)
        } catch (ex: Exception) {
            null
        }) ?: entityClass.getDeclaredConstructor().newInstance()
    }

    fun <Dto : Any> fromDto(dto: Dto): T {
        val dtoClass = dto::class

        // Tentar obter o ID da DTO
        val idFieldName = findIdField<BaseEntity<T>>()
        val dtoIdValue = dtoClass.declaredMemberProperties
            .find { it.name == idFieldName }
            ?.apply { isAccessible = true }
            ?.getter
            ?.call(dto)

        val entity: T = findOrCreateInstance(dtoIdValue)
        return JsonUtils.MAPPER.updateValue(entity, dto)
    }

    fun <T> fetch(query: JPQLQuery<T>, page: Int? = 1, rpp: Int? = 10, fetchCount: Boolean = true): PaginationDto<T> {
        Objects.requireNonNull(query)
        var count: Long = -1

        if (fetchCount) {
            count = query.fetchCount()
        }

        val offset = max(page!!, 1) - 1
        val list = query
            .offset((offset * rpp!!).toLong())
            .limit((rpp + 1).toLong())
            .fetch()

        var hasMore = false
        if (list.size > rpp) {
            list.removeAt(list.size - 1)
            hasMore = true
        }

        return PaginationDto<T>().apply {
            this.page = page
            this.rpp = rpp
            this.hasMore = hasMore
            this.list = list
            this.count = count
        }
    }
}

@MappedSuperclass
abstract class BaseEntity<T> : PanacheEntityBase, Serializable {

    private val simpleName: String
        get() = javaClass.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    var createdAt: OffsetDateTime? = OffsetDateTime.now(ZoneOffset.UTC)

    @Column(name = "created_by")
    var createdBy: String? = null

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    var updatedAt: OffsetDateTime? = null

    @Column(name = "updated_by")
    var updatedBy: String? = null

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null

    abstract val id: Any?

    @PreUpdate
    fun preUpdate() {
        audit()
    }

    private fun audit() {
        val context = ApplicationContext.context
        if (isNewBean) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC)
        }

        if (isNewBean && context != null) {
            createdBy = context.id.toString()
        }

        if (context != null) {
            updatedBy = context.id.toString()
        }

        updatedAt = OffsetDateTime.now(ZoneOffset.UTC)
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
            throw ApplicationException(ConstraintViolationException(errors))
        }
    }

    open fun save(): T {
        return save(false)
    }

    @Suppress("UNCHECKED_CAST")
    open fun save(ignoreValidation: Boolean = false): T {
        if (!ignoreValidation) validate()
        audit()
        if (!isNewBean) return Panache.getEntityManager().merge(this) as T
        this.persist()
        return this as T
    }

}

private val validator = Validation.buildDefaultValidatorFactory().validator