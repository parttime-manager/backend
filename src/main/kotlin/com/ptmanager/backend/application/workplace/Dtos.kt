package com.ptmanager.backend.application.workplace


import com.ptmanager.backend.domain.Workplace
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class CreateWorkplaceRequest(
    @field:NotBlank val name: String,
    val address: String?,
)

/** 직원 시급 설정 요청 (사장). 단위: 원. */
data class UpdateWageRequest(
    @field:Min(0) val hourlyWage: Int,
)

/** 매장 QR 출근 토큰. 직원 앱이 이 토큰을 스캔해 출근 체크에 사용한다. */
data class QrTokenResponse(
    val qrToken: String,
)

data class WorkplaceResponse(
    val id: Long?,
    val name: String,
    val address: String?,
    val inviteCode: String,
    val createdAt: Instant?,
) {
    companion object {
        fun from(entity: Workplace): WorkplaceResponse = WorkplaceResponse(
            id = entity.id,
            name = entity.name,
            address = entity.address,
            inviteCode = entity.inviteCode,
            createdAt = entity.createdAt,
        )
    }
}
