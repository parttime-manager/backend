package com.ptmanager.backend.notification;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptmanager.backend.domain.Notification;
import com.ptmanager.backend.domain.NotificationType;
import com.ptmanager.backend.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> findByUser(long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Notification notify(long userId, NotificationType type, String message) {
        Notification notification = new Notification(null, userId, type, message, false, Instant.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markRead(long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Notification not found."));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
}
