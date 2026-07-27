package com.ptmanager.backend.application.user


import com.ptmanager.backend.domain.DeviceToken
import com.ptmanager.backend.domain.NotificationSetting
import com.ptmanager.backend.domain.Platform
import com.ptmanager.backend.domain.User
import com.ptmanager.backend.domain.UserRole
import jakarta.validation.constraints.NotBlank
import java.time.Instant

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

data class UserResponse(
    val id: Long?,
    val email: String,
    val name: String,
    val role: UserRole,
    val workplaceId: Long?,
    val hourlyWage: Int,
    val lastReadNoticeAt: Instant?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    companion object {
        fun from(entity: User): UserResponse = UserResponse(
            id = entity.id,
            email = entity.email,
            name = entity.name,
            role = entity.role,
            workplaceId = entity.workplaceId,
            hourlyWage = entity.hourlyWage,
            lastReadNoticeAt = entity.lastReadNoticeAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}

data class DeviceTokenResponse(
    val id: Long?,
    val userId: Long,
    val token: String,
    val platform: Platform,
    val updatedAt: Instant?,
) {
    companion object {
        fun from(entity: DeviceToken): DeviceTokenResponse = DeviceTokenResponse(
            id = entity.id,
            userId = entity.userId,
            token = entity.token,
            platform = entity.platform,
            updatedAt = entity.updatedAt,
        )
    }
}

data class NotificationSettingResponse(
    val userId: Long,
    val swapEnabled: Boolean,
    val noticeEnabled: Boolean,
    val attendanceEnabled: Boolean,
    val joinRequestEnabled: Boolean,
) {
    companion object {
        fun from(entity: NotificationSetting): NotificationSettingResponse = NotificationSettingResponse(
            userId = entity.userId,
            swapEnabled = entity.swapEnabled,
            noticeEnabled = entity.noticeEnabled,
            attendanceEnabled = entity.attendanceEnabled,
            joinRequestEnabled = entity.joinRequestEnabled,
        )
    }
}
