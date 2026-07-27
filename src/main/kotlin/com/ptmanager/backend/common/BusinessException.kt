package com.ptmanager.backend.common

enum class BusinessError {
    INVALID_INPUT, AUTHENTICATION_FAILED, ACCESS_DENIED, STATE_CONFLICT,
}

class BusinessException(val error: BusinessError, message: String) : RuntimeException(message)
