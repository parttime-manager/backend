package com.ptmanager.backend.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "app_user")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String = "",

    @field:JsonIgnore
    @Column(nullable = false)
    var password: String = "",

    @Column(nullable = false)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var role: UserRole = UserRole.EMPLOYEE,

    @Column(name = "workplace_id")
    var workplaceId: Long? = null,

    @Column(name = "hourly_wage", nullable = false)
    var hourlyWage: Int = 0,

    @Column(name = "last_read_notice_at")
    var lastReadNoticeAt: Instant? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,
)
