package com.ptmanager.backend.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "device_token")
class DeviceToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,

    @Column(nullable = false, unique = true, length = 512)
    var token: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var platform: Platform = Platform.ANDROID,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,
)
