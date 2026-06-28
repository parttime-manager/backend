package com.ptmanager.backend.notification

import com.ptmanager.backend.domain.Notification
import com.ptmanager.backend.domain.NotificationType
import com.ptmanager.backend.repository.NotificationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
) {

    fun findByUser(userId: Long, isRead: Boolean?): List<Notification> =
        if (isRead == null) {
            notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
        } else {
            notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, isRead)
        }

    fun unreadCount(userId: Long): Long =
        notificationRepository.countByUserIdAndReadFalse(userId)

    @Transactional
    fun notify(
        userId: Long,
        type: NotificationType,
        message: String,
        targetType: String? = null,
        targetId: Long? = null,
    ): Notification {
        val notification = Notification(
            userId = userId,
            type = type,
            message = message,
            targetType = targetType,
            targetId = targetId,
            read = false,
        )
        return notificationRepository.save(notification)
    }

    /** 여러 사용자에게 같은 알림을 발송한다. (대타 요청·공지 등 fan-out) */
    @Transactional
    fun notifyAll(
        userIds: List<Long>,
        type: NotificationType,
        message: String,
        targetType: String? = null,
        targetId: Long? = null,
    ) {
        userIds.forEach { notify(it, type, message, targetType, targetId) }
    }

    @Transactional
    fun markRead(id: Long) {
        val notification = notificationRepository.findById(id)
            .orElseThrow { NoSuchElementException("Notification not found.") }
        notification.read = true
        notificationRepository.save(notification)
    }

    @Transactional
    fun markAllRead(userId: Long) {
        val unread = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false)
        unread.forEach { it.read = true }
        notificationRepository.saveAll(unread)
    }
}
