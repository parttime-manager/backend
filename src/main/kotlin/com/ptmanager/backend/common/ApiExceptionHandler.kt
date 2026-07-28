package com.ptmanager.backend.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.NoSuchElementException

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(exception: NoSuchElementException): ResponseEntity<ApiError> =
        response(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.message)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(exception: IllegalArgumentException): ResponseEntity<ApiError> =
        response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.message)

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(exception: BusinessException): ResponseEntity<ApiError> {
        val status = when (exception.error) {
            BusinessError.INVALID_INPUT -> HttpStatus.BAD_REQUEST
            BusinessError.AUTHENTICATION_FAILED -> HttpStatus.UNAUTHORIZED
            BusinessError.ACCESS_DENIED -> HttpStatus.FORBIDDEN
            BusinessError.STATE_CONFLICT -> HttpStatus.CONFLICT
        }
        return response(status, status.name, exception.message)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(exception: ResponseStatusException): ResponseEntity<ApiError> {
        val status = exception.statusCode
        val code = (status as? HttpStatus)?.name ?: status.toString()
        return response(status, code, exception.reason, headers = exception.headers)
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handlePayloadTooLarge(exception: MaxUploadSizeExceededException): ResponseEntity<ApiError> =
        response(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE", "파일 용량이 초과되었습니다.")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val fields = exception.bindingResult.fieldErrors.associate { error ->
            error.field to (error.defaultMessage ?: "invalid")
        }
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed.", fields)
    }

    private fun response(
        status: org.springframework.http.HttpStatusCode,
        code: String,
        message: String?,
        fields: Map<String, String> = emptyMap(),
        headers: org.springframework.http.HttpHeaders = org.springframework.http.HttpHeaders(),
    ): ResponseEntity<ApiError> = ResponseEntity.status(status)
        .headers(headers)
        .headers { if (status.value() == 401) it.set("WWW-Authenticate", "Bearer") }
        .body(ApiError(code, message, Instant.now(), fields))

    data class ApiError(
        val code: String,
        val message: String?,
        val timestamp: Instant,
        val fields: Map<String, String>,
    )
}
