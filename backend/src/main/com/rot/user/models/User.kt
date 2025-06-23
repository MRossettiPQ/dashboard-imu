package com.rot.user.models

import com.querydsl.core.annotations.Config
import com.rot.access.dtos.AccessDto
import com.rot.core.config.ApplicationConfig
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.core.utils.EncryptUtils
import com.rot.core.utils.JwtType
import com.rot.core.utils.JwtUtils
import com.rot.user.dtos.UserDto
import com.rot.user.enums.UserRole
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.core.Response
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.Transient


@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_active", columnList = "active"),
        Index(name = "idx_user_username", columnList = "username"),
        Index(name = "idx_user_email", columnList = "email"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_user_username", columnNames = ["username"]),
        UniqueConstraint(name = "uk_user_email", columnNames = ["email"])
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class User : BaseEntity<User>() {
    companion object : BaseCompanion<User, UUID, QUser> {
        override val entityClass: Class<User> = User::class.java
        override val q: QUser = QUser.user

        fun findByUsername(username: String): User {
            return createQuery()
                .where(q.username.eq(username))
                .fetchFirst() ?: throw ApplicationException("User not found", Response.Status.NOT_FOUND)
        }

    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @Column(name = "active", nullable = false)
    var active: Boolean = false

    @NotNull
    @NotEmpty
    @Column(name = "username", unique = true, nullable = false, updatable = false)
    var username: String? = null

    @NotNull
    @NotEmpty
    @Column(name = "name", unique = true, nullable = false)
    var name: String? = null

    @Email
    @NotNull
    @NotEmpty
    @Column(name = "email", unique = true, nullable = false)
    var email: String? = null

    @NotNull
    @NotEmpty
    @Column(name = "password", nullable = false)
    var password: String? = null

    @NotNull
    @NotEmpty
    @Column(name = "salt", nullable = false)
    var salt: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: UserRole = UserRole.PHYSIOTHERAPIST

    fun toDto(): UserDto {
        return UserDto.from(this)
    }

    @Transient
    var access: AccessDto? = null

    fun generateToken(issuer: String, subject: String): AccessDto {
        val durationAccess = Duration.ofHours(12)
        val durationRefresh = Duration.ofDays(4)
        val accessTokenExpiresAt = LocalDateTime.now().plusHours(12)
        val refreshTokenExpiresAt = LocalDateTime.now().plusDays(4)

        access = AccessDto()
        access?.accessTokenExpiresAt = accessTokenExpiresAt
        access?.accessToken = JwtUtils.generate(
            issuer = issuer,
            subject = subject,
            type = JwtType.ACCESS,
            groups = mutableSetOf(role.name),
            claims = mutableMapOf(
                "roles" to listOf(role.name),
                "reference" to id
            ),
            duration = durationAccess,
            username = username,
        )

        access?.refreshTokenExpiresAt = refreshTokenExpiresAt
        access?.refreshToken = JwtUtils.generate(
            issuer = issuer,
            subject = subject,
            type = JwtType.REFRESH,
            claims = mutableMapOf(
                "reference" to id
            ),
            duration = durationRefresh,
            username = username,
        )

        return access!!
    }

    fun encryptAndSetPassword(password: String) {
        this.salt = EncryptUtils.generateSalt()
        this.password = EncryptUtils.generateHash(password, this.salt!!)
    }

    fun checkSameUsername() {
        val existingUser = User.createQuery()
            .where(q.username.eq(this.username!!))

        if (!isNewBean) {
            existingUser.where(q.id.ne(this.id!!))
        }

        if (existingUser.fetchFirst() != null) {
            throw ApplicationException("Username is already in use.", Response.Status.CONFLICT)
        }
    }

    fun checkSameEmail() {
        val existingUser = User.createQuery()
            .where(q.email.eq(this.email!!))

        if (!isNewBean) {
            existingUser.where(q.id.ne(this.id!!))
        }

        if (existingUser.fetchFirst() != null) {
            throw ApplicationException("Email is already in use.", Response.Status.CONFLICT)
        }
    }

    override fun save(): User {
        checkSameEmail()
        checkSameUsername()
        return super.save()
    }

}
