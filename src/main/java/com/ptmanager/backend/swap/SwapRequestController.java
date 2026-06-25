package com.ptmanager.backend.swap;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ptmanager.backend.domain.SwapRequest;
import com.ptmanager.backend.domain.SwapRequestStatus;

@RestController
@RequestMapping("/api/swap-requests")
public class SwapRequestController {

    private final SwapRequestService swapRequestService;

    public SwapRequestController(SwapRequestService swapRequestService) {
        this.swapRequestService = swapRequestService;
    }

    @GetMapping
    public List<SwapRequest> findSwapRequests() {
        return swapRequestService.findSwapRequests();
    }

    @PostMapping
    public SwapRequest createSwapRequest(@Valid @RequestBody CreateSwapRequest request) {
        return swapRequestService.createSwapRequest(
            request.requesterId(),
            request.substituteId(),
            request.workDate(),
            request.reason()
        );
    }

    @PatchMapping("/{id}/status")
    public SwapRequest updateStatus(
        @PathVariable long id,
        @Valid @RequestBody UpdateSwapRequestStatus request
    ) {
        return swapRequestService.updateStatus(id, request.status());
    }

    public record CreateSwapRequest(
        @NotNull Long requesterId,
        Long substituteId,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
        @NotBlank String reason
    ) {
    }

    public record UpdateSwapRequestStatus(
        @NotNull SwapRequestStatus status
    ) {
    }
}
