package com.ptmanager.backend.workplace.dto

import jakarta.validation.constraints.NotBlank

data class CreateWorkplaceRequest(
    @field:NotBlank val name: String,
    val address: String?,
)
