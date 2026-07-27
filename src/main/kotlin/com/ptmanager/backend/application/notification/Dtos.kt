package com.ptmanager.backend.application.notification


import com.fasterxml.jackson.annotation.JsonProperty
import com.ptmanager.backend.domain.Notification
import com.ptmanager.backend.domain.NotificationType
import java.time.Instant

/** 안 읽은 알림 개수 (배지). */
data class UnreadCount(
    val count: Long,
)

data class NotificationResponse(
    val id: Long?,
    val userId: Long,
    val type: NotificationType,
    val message: String,
    val targetType: String?,
    val targetId: Long?,
    @get:JsonProperty("isRead") val read: Boolean,
    val createdAt: Instant?,
) {
    companion object {
        fun from(entity: Notification): NotificationResponse = NotificationResponse(
            id = entity.id,
            userId = entity.userId,
            type = entity.type,
            message = entity.message,
            targetType = entity.targetType,
            targetId = entity.targetId,
            read = entity.read,
            createdAt = entity.createdAt,
        )
    }
}
