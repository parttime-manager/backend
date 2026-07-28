package com.ptmanager.backend.adapter.inbound.web.workplace

import com.ptmanager.backend.application.workplace.*

import com.ptmanager.backend.domain.UserRole
import com.ptmanager.backend.application.user.UserResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workplaces")
class WorkplaceController(
    private val workplaceService: WorkplaceService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLOYER')")
    fun createWorkplace(@Valid @RequestBody request: CreateWorkplaceRequest): WorkplaceResponse =
        WorkplaceResponse.from(workplaceService.createWorkplace(request.name, request.address))

    @GetMapping("/{workplaceId}")
    fun getWorkplace(@PathVariable workplaceId: Long): WorkplaceResponse =
        WorkplaceResponse.from(workplaceService.getWorkplace(workplaceId))

    @GetMapping("/{workplaceId}/members")
    fun members(
        @PathVariable workplaceId: Long,
        @RequestParam(required = false) role: UserRole?,
    ): List<UserResponse> = workplaceService.findMembers(workplaceId, role).map(UserResponse::from)

    @PostMapping("/{workplaceId}/qr-token")
    @PreAuthorize("hasRole('EMPLOYER')")
    fun qrToken(@PathVariable workplaceId: Long): QrTokenResponse =
        QrTokenResponse(workplaceService.issueQrToken(workplaceId))

    @PatchMapping("/{workplaceId}/members/{userId}/wage")
    @PreAuthorize("hasRole('EMPLOYER')")
    fun updateMemberWage(
        @PathVariable workplaceId: Long,
        @PathVariable userId: Long,
        @Valid @RequestBody request: UpdateWageRequest,
    ): UserResponse = UserResponse.from(workplaceService.updateMemberWage(workplaceId, userId, request.hourlyWage))

    @DeleteMapping("/{workplaceId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('EMPLOYER')")
    fun removeMember(
        @PathVariable workplaceId: Long,
        @PathVariable userId: Long,
    ) = workplaceService.removeMember(workplaceId, userId)
}
