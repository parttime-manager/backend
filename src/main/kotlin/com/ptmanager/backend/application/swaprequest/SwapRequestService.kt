package com.ptmanager.backend.application.swaprequest


import com.ptmanager.backend.common.BusinessError
import com.ptmanager.backend.common.BusinessException
import com.ptmanager.backend.common.access.WorkplaceAccessGuard
import com.ptmanager.backend.common.nameOf
import com.ptmanager.backend.common.namesOf
import com.ptmanager.backend.common.orNotFound
import com.ptmanager.backend.common.workedMinutes
import com.ptmanager.backend.domain.NotificationType
import com.ptmanager.backend.domain.Shift
import com.ptmanager.backend.domain.SwapApplication
import com.ptmanager.backend.domain.SwapRequest
import com.ptmanager.backend.domain.SwapRequestStatus
import com.ptmanager.backend.domain.UserRole
import com.ptmanager.backend.application.notification.NotificationService
import com.ptmanager.backend.application.port.out.ShiftRepository
import com.ptmanager.backend.application.port.out.SwapApplicationRepository
import com.ptmanager.backend.application.port.out.SwapRequestRepository
import com.ptmanager.backend.application.port.out.UserRepository
import com.ptmanager.backend.application.shift.ShiftResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SwapRequestService(
    private val swapRequestRepository: SwapRequestRepository,
    private val swapApplicationRepository: SwapApplicationRepository,
    private val shiftRepository: ShiftRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    private val accessGuard: WorkplaceAccessGuard,
) {

    @Transactional
    fun createSwapRequest(requesterId: Long, shiftId: Long, reason: String): SwapRequest {
        val shift = shiftRepository.findById(shiftId)
            .orNotFound("Shift not found.")
        if (shift.employeeId != requesterId) {
            throw BusinessException(BusinessError.ACCESS_DENIED, "본인 근무만 대타 요청할 수 있습니다.")
        }
        // 한 근무에 열린(PENDING) 대타 요청은 최대 1건
        if (swapRequestRepository.existsByShiftIdAndStatus(shiftId, SwapRequestStatus.PENDING)) {
            throw BusinessException(BusinessError.STATE_CONFLICT, "해당 근무에 이미 열린 대타 요청이 있습니다.")
        }
        val saved = swapRequestRepository.save(
            SwapRequest(
                workplaceId = shift.workplaceId,
                shiftId = shiftId,
                requesterId = requesterId,
                reason = reason,
                status = SwapRequestStatus.PENDING,
            ),
        )

        // 같은 매장 직원들에게 대타 요청 알림 (요청자 제외)
        val recipients = userRepository.findByWorkplaceIdAndRole(shift.workplaceId, UserRole.EMPLOYEE)
            .mapNotNull { it.id }
            .filter { it != requesterId }
        notificationService.notifyAll(
            recipients,
            NotificationType.SWAP_REQUEST,
            "새 대타 요청이 등록되었습니다.",
            targetType = "SWAP_REQUEST",
            targetId = saved.id,
        )
        return saved
    }

    /** view: open(지원 가능, 본인 제외) | mine(내 요청) | pending(승인 대기) */
    fun findSwapRequests(
        workplaceId: Long,
        view: String,
        currentUserId: Long,
        status: SwapRequestStatus?,
    ): List<SwapRequest> {
        accessGuard.requireMemberOf(workplaceId)
        val all = swapRequestRepository.findByWorkplaceIdOrderByCreatedAtDesc(workplaceId)
        return when (view) {
            "open" -> all.filter { it.status == SwapRequestStatus.PENDING && it.requesterId != currentUserId }
            "mine" -> all.filter { it.requesterId == currentUserId && (status == null || it.status == status) }
            "pending" -> all.filter { it.status == SwapRequestStatus.PENDING }
            else -> all
        }
    }

    fun getDetail(id: Long): SwapRequestDetail {
        val request = getRequest(id)
        accessGuard.requireMemberOf(request.workplaceId)
        val shift = shiftRepository.findById(request.shiftId).orElse(null)
        val applications = swapApplicationRepository.findBySwapRequestId(id)
        return SwapRequestDetail(
            id = request.id,
            workplaceId = request.workplaceId,
            shiftId = request.shiftId,
            requesterId = request.requesterId,
            substituteId = request.substituteId,
            reason = request.reason,
            status = request.status,
            createdAt = request.createdAt,
            shift = shift?.let { ShiftResponse.from(it, userRepository.nameOf(it.employeeId)) },
            applications = toApplicationResponses(applications),
        )
    }

    fun listApplications(swapRequestId: Long): List<SwapApplicationResponse> {
        val request = getRequest(swapRequestId)
        accessGuard.requireMemberOf(request.workplaceId)
        return toApplicationResponses(swapApplicationRepository.findBySwapRequestId(swapRequestId))
    }

    fun myApplications(applicantId: Long, status: SwapRequestStatus?): List<SwapApplicationResponse> {
        val all = swapApplicationRepository.findByApplicantIdOrderByCreatedAtDesc(applicantId)
        val filtered = if (status == null) all else all.filter { it.status == status }
        return toApplicationResponses(filtered)
    }

    @Transactional
    fun apply(swapRequestId: Long, applicantId: Long): SwapApplicationResponse {
        val request = getRequest(swapRequestId)
        if (request.requesterId == applicantId) {
            throw BusinessException(BusinessError.ACCESS_DENIED, "본인 요청에는 지원할 수 없습니다.")
        }
        if (request.status != SwapRequestStatus.PENDING) {
            throw BusinessException(BusinessError.STATE_CONFLICT, "마감된 대타 요청입니다.")
        }
        accessGuard.requireMemberOf(request.workplaceId)
        if (swapApplicationRepository.existsBySwapRequestIdAndApplicantId(swapRequestId, applicantId)) {
            throw BusinessException(BusinessError.STATE_CONFLICT, "이미 지원한 요청입니다.")
        }
        val targetShift = shiftRepository.findById(request.shiftId)
            .orNotFound("Shift not found.")
        // 더블부킹: 확정 근무 + 다른 열린 대타에 낸 PENDING 지원까지 포함해 시간 겹침 검사
        if (hasTimeConflict(targetShift, committedShifts(applicantId))) {
            throw BusinessException(BusinessError.INVALID_INPUT, "같은 시간대에 이미 근무/지원이 있어 지원할 수 없습니다.")
        }
        val saved = swapApplicationRepository.save(
            SwapApplication(swapRequestId = swapRequestId, applicantId = applicantId),
        )
        return toApplicationResponse(saved, userRepository.nameOf(applicantId))
    }

    @Transactional
    fun approve(swapRequestId: Long, applicantId: Long): SwapRequest {
        val request = getRequest(swapRequestId)
        accessGuard.requireMemberOf(request.workplaceId)

        val targetShift = shiftRepository.findById(request.shiftId)
            .orNotFound("Shift not found.")
        // 승인 시점 재검증: 대타자의 확정 근무와 시간이 겹치면 승인 불가
        val assignedShifts = shiftRepository.findByEmployeeIdOrderByWorkDateAscStartTimeAsc(applicantId)
        if (hasTimeConflict(targetShift, assignedShifts)) {
            throw BusinessException(BusinessError.INVALID_INPUT, "대타자에게 같은 시간대 근무가 있어 승인할 수 없습니다.")
        }

        // 원자적 동시성 가드: PENDING일 때만 APPROVED로 전이 (영향 행 0이면 이미 처리됨)
        val updated = swapRequestRepository.markApproved(
            swapRequestId, applicantId, SwapRequestStatus.APPROVED, SwapRequestStatus.PENDING,
        )
        if (updated == 0) {
            throw BusinessException(BusinessError.STATE_CONFLICT, "이미 처리된 대타 요청입니다.")
        }
        // markApproved가 영속성 컨텍스트를 clear 하므로 재조회한다.
        val approved = getRequest(swapRequestId)

        val applications = swapApplicationRepository.findBySwapRequestId(swapRequestId)
        applications.forEach {
            it.status = if (it.applicantId == applicantId) SwapRequestStatus.APPROVED else SwapRequestStatus.REJECTED
        }

        // 근무자를 대타자로 재배정 (requester_id 는 이력 보존을 위해 변경하지 않음)
        shiftRepository.findById(approved.shiftId).ifPresent { shift ->
            shift.employeeId = applicantId
        }

        notificationService.notify(
            approved.requesterId, NotificationType.SWAP_RESULT, "대타 요청이 승인되었습니다.",
            targetType = "SWAP_REQUEST", targetId = approved.id,
        )
        notificationService.notify(
            applicantId, NotificationType.SWAP_RESULT, "대타 지원이 승인되었습니다.",
            targetType = "SWAP_REQUEST", targetId = approved.id,
        )
        return approved
    }

    @Transactional
    fun reject(id: Long): SwapRequest {
        val request = getRequest(id)
        accessGuard.requireMemberOf(request.workplaceId)

        val updated = swapRequestRepository.markStatus(
            id, SwapRequestStatus.REJECTED, SwapRequestStatus.PENDING,
        )
        if (updated == 0) {
            throw BusinessException(BusinessError.STATE_CONFLICT, "이미 처리된 대타 요청입니다.")
        }
        val rejected = getRequest(id)
        notificationService.notify(
            rejected.requesterId, NotificationType.SWAP_RESULT, "대타 요청이 거절되었습니다.",
            targetType = "SWAP_REQUEST", targetId = rejected.id,
        )
        return rejected
    }

    /** 지원자가 이미 묶여 있는 근무들: 확정 배정 근무 + 다른 열린 대타에 낸 PENDING 지원의 대상 근무. */
    private fun committedShifts(employeeId: Long): List<Shift> {
        val assigned = shiftRepository.findByEmployeeIdOrderByWorkDateAscStartTimeAsc(employeeId)
        val pendingRequestIds = swapApplicationRepository.findByApplicantIdOrderByCreatedAtDesc(employeeId)
            .filter { it.status == SwapRequestStatus.PENDING }
            .map { it.swapRequestId }
            .distinct()
        // findById 반복 대신 두 번의 배치 조회로 대상 근무를 모은다.
        val pendingShiftIds = swapRequestRepository.findAllById(pendingRequestIds).map { it.shiftId }
        val pendingAppShifts = shiftRepository.findAllById(pendingShiftIds)
        return assigned + pendingAppShifts
    }

    /** target 과 시간이 겹치는 근무가 others 안에 있는지. 같은 work_date, 야간 교대(end≤start)는 익일 보정. */
    private fun hasTimeConflict(target: Shift, others: List<Shift>): Boolean {
        val targetStart = (target.startTime.toSecondOfDay() / 60).toLong()
        val targetEnd = targetStart + workedMinutes(target.startTime, target.endTime)
        return others.any {
            val start = (it.startTime.toSecondOfDay() / 60).toLong()
            it.workDate == target.workDate && it.id != target.id &&
                targetStart < start + workedMinutes(it.startTime, it.endTime) && start < targetEnd
        }
    }

    private fun getRequest(id: Long): SwapRequest =
        swapRequestRepository.findById(id).orNotFound("Swap request not found.")

    private fun toApplicationResponses(applications: List<SwapApplication>): List<SwapApplicationResponse> {
        val names = userRepository.namesOf(applications.map { it.applicantId })
        return applications.map { toApplicationResponse(it, names[it.applicantId]) }
    }

    // applicantName은 호출부에서 명시적으로 넘긴다. (기본인자에서 DB를 치던 N+1 함정 제거)
    private fun toApplicationResponse(
        application: SwapApplication,
        applicantName: String?,
    ): SwapApplicationResponse =
        SwapApplicationResponse(
            id = application.id,
            swapRequestId = application.swapRequestId,
            applicantId = application.applicantId,
            applicantName = applicantName,
            status = application.status,
            createdAt = application.createdAt,
        )
}
