package com.ptmanager.backend.workplace.dto

import jakarta.validation.constraints.Min

/** 직원 시급 설정 요청 (사장). 단위: 원. */
data class UpdateWageRequest(
    @field:Min(0) val hourlyWage: Int,
)
