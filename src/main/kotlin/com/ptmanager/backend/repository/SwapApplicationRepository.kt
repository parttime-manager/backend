package com.ptmanager.backend.repository

import com.ptmanager.backend.domain.SwapApplication
import org.springframework.data.jpa.repository.JpaRepository

interface SwapApplicationRepository : JpaRepository<SwapApplication, Long> {

    fun findBySwapRequestId(swapRequestId: Long): List<SwapApplication>

    fun findByApplicantIdOrderByCreatedAtDesc(applicantId: Long): List<SwapApplication>

    fun existsBySwapRequestIdAndApplicantId(swapRequestId: Long, applicantId: Long): Boolean

    fun deleteBySwapRequestIdIn(swapRequestIds: Collection<Long>)
}
