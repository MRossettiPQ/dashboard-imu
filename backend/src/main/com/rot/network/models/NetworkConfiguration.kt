package com.rot.network.models

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
@Table(name = "network_configurations")
@Config(listAccessors = true, entityAccessors = true, mapAccessors = true)
class NetworkConfiguration : BaseEntity<NetworkConfiguration>() {
    companion object : BaseCompanion<NetworkConfiguration, UUID, QNetworkConfiguration> {
        override val entityClass: Class<NetworkConfiguration> = NetworkConfiguration::class.java
        override val q: QNetworkConfiguration = QNetworkConfiguration.networkConfiguration
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    override var id: UUID? = null

    @Column(name = "active", nullable = false)
    var active: Boolean = false

    @NotNull
    @NotEmpty
    @Column(name = "ip", unique = true, nullable = false, updatable = false)
    var ip: String? = null

    @NotNull
    @NotEmpty
    @Column(name = "ssid", unique = true, nullable = false, updatable = false)
    var ssid: String? = null

    @NotNull
    @NotEmpty
    @Column(name = "password", nullable = false)
    var password: String? = null

    @get:Transient
    val decryptedPassword: String?
        get() = password?.let {
            val key = EncryptUtils.loadPrivateKey("/META-INF/resources/encrypt/private.key")
            EncryptUtils.decryptWithPrivateKey(password!!, key)
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

    override fun save(): NetworkConfiguration {
        validate()
        checkSameSsid()
        return super.save()
    }

}
