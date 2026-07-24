package com.ptmanager.backend.user

import com.ptmanager.backend.domain.Platform
import jakarta.validation.constraints.NotBlank

data class UpdateProfileRequest(
    @field:NotBlank val name: String,
)

data class RegisterDeviceTokenRequest(
    @field:NotBlank val token: String,
    val platform: Platform = Platform.ANDROID,
)

/** 알림 카테고리별 on/off 부분 수정. null인 필드는 변경하지 않는다. */
data class NotificationSettingUpdate(
    val swapEnabled: Boolean? = null,
    val noticeEnabled: Boolean? = null,
    val attendanceEnabled: Boolean? = null,
    val joinRequestEnabled: Boolean? = null,
)
