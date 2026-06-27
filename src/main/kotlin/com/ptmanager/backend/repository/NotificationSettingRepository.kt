package com.ptmanager.backend.repository

import com.ptmanager.backend.domain.NotificationSetting
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationSettingRepository : JpaRepository<NotificationSetting, Long>
