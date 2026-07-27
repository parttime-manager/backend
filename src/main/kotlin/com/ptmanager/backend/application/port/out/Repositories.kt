package com.ptmanager.backend.application.port.out

import com.ptmanager.backend.domain.AttendanceStatus
import com.ptmanager.backend.domain.DeviceToken
import com.ptmanager.backend.domain.HandoverCategory
import com.ptmanager.backend.domain.HandoverNote
import com.ptmanager.backend.domain.JoinRequest
import com.ptmanager.backend.domain.JoinRequestStatus
import com.ptmanager.backend.domain.Notice
import com.ptmanager.backend.domain.NoticeAttachment
import com.ptmanager.backend.domain.Notification
import com.ptmanager.backend.domain.NotificationSetting
import com.ptmanager.backend.domain.Shift
import com.ptmanager.backend.domain.SwapApplication
import com.ptmanager.backend.domain.SwapRequest
import com.ptmanager.backend.domain.SwapRequestStatus
import com.ptmanager.backend.domain.User
import com.ptmanager.backend.domain.UserRole
import com.ptmanager.backend.domain.Workplace
import com.ptmanager.backend.application.port.out.PageResult
import com.ptmanager.backend.application.port.out.PageQuery
import java.time.LocalDate

interface UserRepository : EntityStore<User, Long> {

    fun findByEmailIgnoreCase(email: String): User?

    fun findByWorkplaceId(workplaceId: Long): List<User>

    fun findByWorkplaceIdAndRole(workplaceId: Long, role: UserRole): List<User>
}

interface WorkplaceRepository : EntityStore<Workplace, Long> {

    fun findByInviteCode(inviteCode: String): Workplace?

    fun existsByInviteCode(inviteCode: String): Boolean
}

interface ShiftRepository : EntityStore<Shift, Long> {

    fun findByEmployeeIdOrderByWorkDateAscStartTimeAsc(employeeId: Long): List<Shift>

    fun findByEmployeeIdAndWorkDate(employeeId: Long, workDate: LocalDate): List<Shift>

    fun findByAttendanceStatusAndWorkDateLessThanEqual(
        status: AttendanceStatus,
        workDate: LocalDate,
    ): List<Shift>

    fun findByWorkplaceIdOrderByWorkDateAscStartTimeAsc(workplaceId: Long): List<Shift>

    fun findByWorkplaceIdAndWorkDateBetween(workplaceId: Long, from: LocalDate, to: LocalDate): List<Shift>
}

interface JoinRequestRepository : EntityStore<JoinRequest, Long> {

    fun findByWorkplaceIdAndStatusOrderByCreatedAtDesc(
        workplaceId: Long,
        status: JoinRequestStatus,
    ): List<JoinRequest>

    fun existsByWorkplaceIdAndUserIdAndStatus(
        workplaceId: Long,
        userId: Long,
        status: JoinRequestStatus,
    ): Boolean

    /** 상태 전이(승인/거절): expectedStatus(PENDING)일 때만 원자적으로 갱신. 영향 행 0이면 이미 처리됨. */
    
    
    fun markStatus(
         id: Long,
         newStatus: JoinRequestStatus,
         expectedStatus: JoinRequestStatus,
    ): Int
}

interface SwapRequestRepository : EntityStore<SwapRequest, Long> {

    fun findByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long): List<SwapRequest>

    fun findByShiftId(shiftId: Long): List<SwapRequest>

    fun existsByShiftIdAndStatus(shiftId: Long, status: SwapRequestStatus): Boolean

    /** 승인: status가 expectedStatus(PENDING)일 때만 원자적으로 갱신. 영향 행 0이면 이미 처리됨. */
    
    
    fun markApproved(
         id: Long,
         applicantId: Long,
         newStatus: SwapRequestStatus,
         expectedStatus: SwapRequestStatus,
    ): Int

    /** 상태 전이(거절 등): expectedStatus일 때만 원자적으로 갱신. */
    
    
    fun markStatus(
         id: Long,
         newStatus: SwapRequestStatus,
         expectedStatus: SwapRequestStatus,
    ): Int
}

interface SwapApplicationRepository : EntityStore<SwapApplication, Long> {

    fun findBySwapRequestId(swapRequestId: Long): List<SwapApplication>

    fun findByApplicantIdOrderByCreatedAtDesc(applicantId: Long): List<SwapApplication>

    fun existsBySwapRequestIdAndApplicantId(swapRequestId: Long, applicantId: Long): Boolean

    fun deleteBySwapRequestIdIn(swapRequestIds: Collection<Long>)
}

interface NoticeRepository : EntityStore<Notice, Long> {

    fun findByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long, pageable: PageQuery): PageResult<Notice>

    fun findFirstByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long): Notice?
}

interface NoticeAttachmentRepository : EntityStore<NoticeAttachment, Long> {

    fun findByNoticeId(noticeId: Long): List<NoticeAttachment>

    fun findByNoticeIdIn(noticeIds: Collection<Long>): List<NoticeAttachment>

    fun deleteByNoticeId(noticeId: Long)
}

interface HandoverNoteRepository : EntityStore<HandoverNote, Long> {

    fun findByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long): List<HandoverNote>

    fun findByWorkplaceIdAndCategoryOrderByCreatedAtDesc(
        workplaceId: Long,
        category: HandoverCategory,
    ): List<HandoverNote>
}

interface NotificationRepository : EntityStore<Notification, Long> {

    fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: PageQuery): PageResult<Notification>

    fun findByUserIdAndReadOrderByCreatedAtDesc(userId: Long, read: Boolean, pageable: PageQuery): PageResult<Notification>

    /** markAllRead 용 (페이지네이션 없이 미읽음 전체). */
    fun findByUserIdAndRead(userId: Long, read: Boolean): List<Notification>

    fun countByUserIdAndReadFalse(userId: Long): Long
}

interface NotificationSettingRepository : EntityStore<NotificationSetting, Long>

interface DeviceTokenRepository : EntityStore<DeviceToken, Long> {

    fun findByToken(token: String): DeviceToken?

    fun findByUserIdIn(userIds: Collection<Long>): List<DeviceToken>

    fun deleteByTokenIn(tokens: Collection<String>)
}
