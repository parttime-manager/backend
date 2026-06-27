package com.ptmanager.backend.swaprequest

import com.ptmanager.backend.domain.NotificationType
import com.ptmanager.backend.domain.SwapRequest
import com.ptmanager.backend.domain.SwapRequestStatus
import com.ptmanager.backend.notification.NotificationService
import com.ptmanager.backend.repository.ShiftRepository
import com.ptmanager.backend.repository.SwapRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

@Service
class SwapRequestService(
    private val swapRequestRepository: SwapRequestRepository,
    private val shiftRepository: ShiftRepository,
    private val notificationService: NotificationService,
) {

    fun findSwapRequests(): List<SwapRequest> = swapRequestRepository.findAllByOrderByCreatedAtDesc()

    @Transactional
    fun createSwapRequest(requesterId: Long, shiftId: Long, reason: String): SwapRequest {
        val shift = shiftRepository.findById(shiftId)
            .orElseThrow { NoSuchElementException("Shift not found.") }
        require(shift.employeeId == requesterId) { "Can only request a swap for your own shift." }

        val request = SwapRequest(
            workplaceId = shift.workplaceId,
            shiftId = shiftId,
            requesterId = requesterId,
            reason = reason,
            status = SwapRequestStatus.PENDING,
        )
        return swapRequestRepository.save(request)
    }

    @Transactional
    fun updateStatus(id: Long, status: SwapRequestStatus): SwapRequest {
        val request = swapRequestRepository.findById(id)
            .orElseThrow { NoSuchElementException("Swap request not found.") }
        request.status = status
        val saved = swapRequestRepository.save(request)
        notificationService.notify(
            saved.requesterId,
            NotificationType.SWAP_RESULT,
            "대타 요청이 ${status.name} 처리되었습니다.",
        )
        return saved
    }
}
