package com.ptmanager.backend.notification;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ptmanager.backend.domain.Notification;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> findNotifications(@RequestParam long userId) {
        return notificationService.findByUser(userId);
    }

    @PatchMapping("/{id}/read")
    public Notification markRead(@PathVariable long id) {
        return notificationService.markRead(id);
    }
}
