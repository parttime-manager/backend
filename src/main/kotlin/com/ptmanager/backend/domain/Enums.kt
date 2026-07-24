package com.ptmanager.backend.domain

enum class UserRole { EMPLOYEE, EMPLOYER }

enum class Platform { ANDROID, IOS }

enum class JoinRequestStatus { PENDING, APPROVED, REJECTED }

enum class SwapRequestStatus { PENDING, APPROVED, REJECTED }

enum class AttendanceStatus { SCHEDULED, PRESENT, LATE, ABSENT }

/** 인수인계 노트 분류. (#재고 / #기기오류 / #손님) */
enum class HandoverCategory { STOCK, DEVICE, CUSTOMER }

enum class NotificationType {
    JOIN_REQUEST,
    SWAP_REQUEST,
    SWAP_APPLICATION,
    SWAP_RESULT,
    ATTENDANCE,
    SCHEDULE_CHANGED,
    NOTICE,
}
