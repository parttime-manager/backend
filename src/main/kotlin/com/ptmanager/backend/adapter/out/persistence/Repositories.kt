package com.ptmanager.backend.adapter.out.persistence

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
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface UserRepository : JpaEntityStore<User, Long>, com.ptmanager.backend.application.port.out.UserRepository {

    override fun findByEmailIgnoreCase(email: String): User?

    override fun findByWorkplaceId(workplaceId: Long): List<User>

    override fun findByWorkplaceIdAndRole(workplaceId: Long, role: UserRole): List<User>
}

interface WorkplaceRepository : JpaEntityStore<Workplace, Long>, com.ptmanager.backend.application.port.out.WorkplaceRepository {

    override fun findByInviteCode(inviteCode: String): Workplace?

    override fun existsByInviteCode(inviteCode: String): Boolean
}

interface ShiftRepository : JpaEntityStore<Shift, Long>, com.ptmanager.backend.application.port.out.ShiftRepository {

    override fun findByEmployeeIdOrderByWorkDateAscStartTimeAsc(employeeId: Long): List<Shift>

    override fun findByEmployeeIdAndWorkDate(employeeId: Long, workDate: LocalDate): List<Shift>

    override fun findByAttendanceStatusAndWorkDateLessThanEqual(
        status: AttendanceStatus,
        workDate: LocalDate,
    ): List<Shift>

    override fun findByWorkplaceIdOrderByWorkDateAscStartTimeAsc(workplaceId: Long): List<Shift>

    override fun findByWorkplaceIdAndWorkDateBetween(workplaceId: Long, from: LocalDate, to: LocalDate): List<Shift>
}

interface JoinRequestRepository : JpaEntityStore<JoinRequest, Long>, com.ptmanager.backend.application.port.out.JoinRequestRepository {

    override fun findByWorkplaceIdAndStatusOrderByCreatedAtDesc(
        workplaceId: Long,
        status: JoinRequestStatus,
    ): List<JoinRequest>

    override fun existsByWorkplaceIdAndUserIdAndStatus(
        workplaceId: Long,
        userId: Long,
        status: JoinRequestStatus,
    ): Boolean

    /** 상태 전이(승인/거절): expectedStatus(PENDING)일 때만 원자적으로 갱신. 영향 행 0이면 이미 처리됨. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update JoinRequest j set j.status = :newStatus where j.id = :id and j.status = :expectedStatus")
    override fun markStatus(
        @Param("id") id: Long,
        @Param("newStatus") newStatus: JoinRequestStatus,
        @Param("expectedStatus") expectedStatus: JoinRequestStatus,
    ): Int
}

interface SwapRequestRepository : JpaEntityStore<SwapRequest, Long>, com.ptmanager.backend.application.port.out.SwapRequestRepository {

    override fun findByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long): List<SwapRequest>

    override fun findByShiftId(shiftId: Long): List<SwapRequest>

    override fun existsByShiftIdAndStatus(shiftId: Long, status: SwapRequestStatus): Boolean

    /** 승인: status가 expectedStatus(PENDING)일 때만 원자적으로 갱신. 영향 행 0이면 이미 처리됨. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        "update SwapRequest s set s.status = :newStatus, s.substituteId = :applicantId " +
            "where s.id = :id and s.status = :expectedStatus",
    )
    override fun markApproved(
        @Param("id") id: Long,
        @Param("applicantId") applicantId: Long,
        @Param("newStatus") newStatus: SwapRequestStatus,
        @Param("expectedStatus") expectedStatus: SwapRequestStatus,
    ): Int

    /** 상태 전이(거절 등): expectedStatus일 때만 원자적으로 갱신. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update SwapRequest s set s.status = :newStatus where s.id = :id and s.status = :expectedStatus")
    override fun markStatus(
        @Param("id") id: Long,
        @Param("newStatus") newStatus: SwapRequestStatus,
        @Param("expectedStatus") expectedStatus: SwapRequestStatus,
    ): Int
}

interface SwapApplicationRepository : JpaEntityStore<SwapApplication, Long>, com.ptmanager.backend.application.port.out.SwapApplicationRepository {

    override fun findBySwapRequestId(swapRequestId: Long): List<SwapApplication>

    override fun findByApplicantIdOrderByCreatedAtDesc(applicantId: Long): List<SwapApplication>

    override fun existsBySwapRequestIdAndApplicantId(swapRequestId: Long, applicantId: Long): Boolean

    override fun deleteBySwapRequestIdIn(swapRequestIds: Collection<Long>)
}

interface NoticeRepository : JpaEntityStore<Notice, Long> {

    fun findByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long, pageable: Pageable): Page<Notice>

    fun findFirstByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long): Notice?
}

interface NoticeAttachmentRepository : JpaEntityStore<NoticeAttachment, Long>, com.ptmanager.backend.application.port.out.NoticeAttachmentRepository {

    override fun findByNoticeId(noticeId: Long): List<NoticeAttachment>

    override fun findByNoticeIdIn(noticeIds: Collection<Long>): List<NoticeAttachment>

    override fun deleteByNoticeId(noticeId: Long)
}

interface HandoverNoteRepository : JpaEntityStore<HandoverNote, Long>, com.ptmanager.backend.application.port.out.HandoverNoteRepository {

    override fun findByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long): List<HandoverNote>

    override fun findByWorkplaceIdAndCategoryOrderByCreatedAtDesc(
        workplaceId: Long,
        category: HandoverCategory,
    ): List<HandoverNote>
}

interface NotificationRepository : JpaEntityStore<Notification, Long> {

    fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<Notification>

    fun findByUserIdAndReadOrderByCreatedAtDesc(userId: Long, read: Boolean, pageable: Pageable): Page<Notification>

    /** markAllRead 용 (페이지네이션 없이 미읽음 전체). */
    fun findByUserIdAndRead(userId: Long, read: Boolean): List<Notification>

    fun countByUserIdAndReadFalse(userId: Long): Long
}

interface NotificationSettingRepository : JpaEntityStore<NotificationSetting, Long>, com.ptmanager.backend.application.port.out.NotificationSettingRepository

interface DeviceTokenRepository : JpaEntityStore<DeviceToken, Long>, com.ptmanager.backend.application.port.out.DeviceTokenRepository {

    override fun findByToken(token: String): DeviceToken?

    override fun findByUserIdIn(userIds: Collection<Long>): List<DeviceToken>

    override fun deleteByTokenIn(tokens: Collection<String>)
}
