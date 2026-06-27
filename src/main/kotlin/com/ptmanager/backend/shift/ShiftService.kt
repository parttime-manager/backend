package com.ptmanager.backend.shift

import com.ptmanager.backend.domain.AttendanceStatus
import com.ptmanager.backend.domain.Shift
import com.ptmanager.backend.repository.ShiftRepository
import com.ptmanager.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId
import java.util.NoSuchElementException

@Service
class ShiftService(
    private val shiftRepository: ShiftRepository,
    private val userRepository: UserRepository,
) {

    fun findShiftsByUser(userId: Long): List<Shift> {
        if (!userRepository.existsById(userId)) {
            throw NoSuchElementException("User not found.")
        }
        return shiftRepository.findByEmployeeIdOrderByWorkDateAscStartTimeAsc(userId)
    }

    @Transactional
    fun checkIn(shiftId: Long): Shift {
        val shift = shiftRepository.findById(shiftId)
            .orElseThrow { NoSuchElementException("Shift not found.") }
        require(shift.checkedInAt == null) { "Shift is already checked in." }

        val now = Instant.now()
        shift.checkedInAt = now
        // checked_in_at 과 start_time 비교로 PRESENT/LATE 판정 (결근 ABSENT는 배치가 담당)
        val scheduledStart = shift.workDate.atTime(shift.startTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
        shift.attendanceStatus =
            if (now.isAfter(scheduledStart)) AttendanceStatus.LATE else AttendanceStatus.PRESENT

        return shiftRepository.save(shift)
    }
}
