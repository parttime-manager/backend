package com.ptmanager.backend.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "notification_setting")
class NotificationSetting(

    @Id
    @Column(name = "user_id")
    var userId: Long = 0,

    @Column(name = "swap_enabled", nullable = false)
    var swapEnabled: Boolean = true,

    @Column(name = "notice_enabled", nullable = false)
    var noticeEnabled: Boolean = true,

    @Column(name = "attendance_enabled", nullable = false)
    var attendanceEnabled: Boolean = true,

    @Column(name = "join_request_enabled", nullable = false)
    var joinRequestEnabled: Boolean = true,
)
