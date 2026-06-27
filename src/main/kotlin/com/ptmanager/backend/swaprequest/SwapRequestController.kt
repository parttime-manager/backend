package com.ptmanager.backend.swaprequest

import com.ptmanager.backend.domain.SwapRequest
import com.ptmanager.backend.swaprequest.dto.CreateSwapRequest
import com.ptmanager.backend.swaprequest.dto.UpdateSwapRequestStatus
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/swap-requests")
class SwapRequestController(
    private val swapRequestService: SwapRequestService,
) {

    @GetMapping
    fun findSwapRequests(): List<SwapRequest> = swapRequestService.findSwapRequests()

    @PostMapping
    fun createSwapRequest(@Valid @RequestBody request: CreateSwapRequest): SwapRequest =
        swapRequestService.createSwapRequest(
            request.requesterId,
            request.shiftId,
            request.reason,
        )

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateSwapRequestStatus,
    ): SwapRequest = swapRequestService.updateStatus(id, request.status)
}
