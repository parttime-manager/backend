package com.ptmanager.backend.auth.dto

import com.ptmanager.backend.domain.User

data class LoginResponse(
    val accessToken: String,
    val user: User,
)
