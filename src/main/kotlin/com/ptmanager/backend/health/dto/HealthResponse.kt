package com.ptmanager.backend.health.dto

import java.time.Instant

data class HealthResponse(
    val status: String,
    val timestamp: Instant,
)
