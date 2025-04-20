package com.rot.user.models

import com.querydsl.core.annotations.Config
import com.rot.core.config.ApplicationConfig
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.core.utils.EncryptUtils
import com.rot.core.utils.JwtUtils
import com.rot.user.enums.UserRole
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.Duration
import java.util.*


@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_active", columnList = "active"),
        Index(name = "idx_user_username", columnList = "username"),
        Index(name = "idx_user_email", columnList = "email"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class User : BaseEntity<User>() {
    companion object : BaseCompanion<User, UUID, QUser> {
        override val entityClass: Class<User> = User::class.java
        override val q: QUser = QUser.user
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
    @Column(name = "name", nullable = false)
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

    fun generateToken(): String {
        return JwtUtils.generate(
            issuer = ApplicationConfig.config.security().issuer(),
            subject = ApplicationConfig.config.security().subject(),
            groups = mutableSetOf(role.name),
            claims = mutableMapOf(
                "roles" to listOf(role.name),
                "reference" to id
            ),
            duration = Duration.ofHours(12),
            audience = ApplicationConfig.config.security().issuer(),
            username = username,
        )
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
            throw ApplicationException("Username is already in use.", 400)
        }
    }

    fun checkSameEmail() {
        val existingUser = User.createQuery()
            .where(q.email.eq(this.email!!))

        if (!isNewBean) {
            existingUser.where(q.id.ne(this.id!!))
        }

        if (existingUser.fetchFirst() != null) {
            throw ApplicationException("Email is already in use.", 400)
        }
    }

    override fun save(): User {
        checkSameEmail()
        checkSameUsername()
        return super.save()
    }

}
