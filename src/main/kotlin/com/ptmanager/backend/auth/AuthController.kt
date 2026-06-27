package com.ptmanager.backend.auth

import com.ptmanager.backend.auth.dto.LoginRequest
import com.ptmanager.backend.auth.dto.LoginResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse {
        val user = authService.login(request.email, request.password)
        return LoginResponse("dev-token-" + user.id, user)
    }
}
