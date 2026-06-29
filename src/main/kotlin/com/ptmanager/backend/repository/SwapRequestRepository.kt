package com.ptmanager.backend.repository

import com.ptmanager.backend.domain.SwapRequest
import com.ptmanager.backend.domain.SwapRequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SwapRequestRepository : JpaRepository<SwapRequest, Long> {

    fun findByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long): List<SwapRequest>

    fun findByShiftId(shiftId: Long): List<SwapRequest>

    fun existsByShiftIdAndStatus(shiftId: Long, status: SwapRequestStatus): Boolean

    /** 승인: status가 expectedStatus(PENDING)일 때만 원자적으로 갱신. 영향 행 0이면 이미 처리됨. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        "update SwapRequest s set s.status = :newStatus, s.substituteId = :applicantId " +
            "where s.id = :id and s.status = :expectedStatus",
    )
    fun markApproved(
        @Param("id") id: Long,
        @Param("applicantId") applicantId: Long,
        @Param("newStatus") newStatus: SwapRequestStatus,
        @Param("expectedStatus") expectedStatus: SwapRequestStatus,
    ): Int

    /** 상태 전이(거절 등): expectedStatus일 때만 원자적으로 갱신. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update SwapRequest s set s.status = :newStatus where s.id = :id and s.status = :expectedStatus")
    fun markStatus(
        @Param("id") id: Long,
        @Param("newStatus") newStatus: SwapRequestStatus,
        @Param("expectedStatus") expectedStatus: SwapRequestStatus,
    ): Int
}
