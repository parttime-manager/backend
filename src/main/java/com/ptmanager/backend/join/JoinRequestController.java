package com.ptmanager.backend.join;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ptmanager.backend.domain.JoinRequest;
import com.ptmanager.backend.domain.JoinRequestStatus;

@RestController
@RequestMapping("/api/join-requests")
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    public JoinRequestController(JoinRequestService joinRequestService) {
        this.joinRequestService = joinRequestService;
    }

    @GetMapping
    public List<JoinRequest> findJoinRequests(@RequestParam long workplaceId) {
        return joinRequestService.findByWorkplace(workplaceId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JoinRequest createJoinRequest(@Valid @RequestBody CreateJoinRequest request) {
        return joinRequestService.create(request.inviteCode(), request.userId());
    }

    @PatchMapping("/{id}/status")
    public JoinRequest updateStatus(
        @PathVariable long id,
        @Valid @RequestBody UpdateJoinRequestStatus request
    ) {
        return joinRequestService.updateStatus(id, request.status());
    }

    public record CreateJoinRequest(
        @NotBlank String inviteCode,
        @NotNull Long userId
    ) {
    }

    public record UpdateJoinRequestStatus(
        @NotNull JoinRequestStatus status
    ) {
    }
}
