package com.rot.user.models

import com.querydsl.core.annotations.Config
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.core.hibernate.structures.BaseCompanion
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.*


@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_active", columnList = "active"),
        Index(name = "idx_user_username", columnList = "username"),
    ]
)
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class User : BaseEntity<User>() {
    companion object: BaseCompanion<User, UUID> {
        override val entityClass: Class<User> = User::class.java
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
}
