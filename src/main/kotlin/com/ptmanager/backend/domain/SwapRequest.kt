package com.ptmanager.backend.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "swap_request")
class SwapRequest(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "workplace_id", nullable = false)
    var workplaceId: Long = 0,

    @Column(name = "shift_id", nullable = false)
    var shiftId: Long = 0,

    @Column(name = "requester_id", nullable = false)
    var requesterId: Long = 0,

    @Column(name = "substitute_id")
    var substituteId: Long? = null,

    @Column(nullable = false, length = 500)
    var reason: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var status: SwapRequestStatus = SwapRequestStatus.PENDING,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
)
