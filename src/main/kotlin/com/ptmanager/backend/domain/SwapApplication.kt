package com.ptmanager.backend.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "swap_application",
    uniqueConstraints = [UniqueConstraint(columnNames = ["swap_request_id", "applicant_id"])],
)
class SwapApplication(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "swap_request_id", nullable = false)
    var swapRequestId: Long = 0,

    @Column(name = "applicant_id", nullable = false)
    var applicantId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var status: SwapRequestStatus = SwapRequestStatus.PENDING,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
)
