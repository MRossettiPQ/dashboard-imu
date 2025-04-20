package com.rot.wifi.models

import com.querydsl.core.annotations.Config
import com.rot.core.exceptions.ApplicationException
import com.rot.core.hibernate.structures.BaseCompanion
import com.rot.core.hibernate.structures.BaseEntity
import com.rot.core.utils.EncryptUtils
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.*


@Entity
@Table(name = "wifi")
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class Wifi : BaseEntity<Wifi>() {
    companion object : BaseCompanion<Wifi, UUID, QWifi> {
        override val entityClass: Class<Wifi> = Wifi::class.java
        override val q: QWifi = QWifi.wifi
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @NotNull
    @NotEmpty
    @Column(name = "ssid", unique = true, nullable = false, updatable = false)
    var ssid: String? = null

    @NotNull
    @NotEmpty
    @Column(name = "password", nullable = false)
    var password: String? = null

    @Transient
    var decryptedPassword: String? = null
        get() {
            return password?.let {
                val key = EncryptUtils.loadPrivateKey("/META-INF/resources/encrypt/private.key")
                EncryptUtils.decryptWithPrivateKey(password!!, key)
            }
        }

    fun encryptAndSetPassword(password: String) {
        val key = EncryptUtils.loadPublicKey("/META-INF/resources/encrypt/public.key")
        this.password = EncryptUtils.encryptWithPublicKey(password, key)
    }

    fun checkSameSsid() {
        val existing = createQuery()
            .where(q.ssid.eq(this.ssid!!))

        if (!isNewBean) {
            existing.where(q.id.ne(this.id!!))
        }

        if (existing.fetchFirst() != null) {
            throw ApplicationException("Existing SSID")
        }
    }

    override fun save(): Wifi {
        validate()
        checkSameSsid()
        return super.save()
    }

}
