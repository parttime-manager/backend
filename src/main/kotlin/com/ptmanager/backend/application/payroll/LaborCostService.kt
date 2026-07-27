package com.ptmanager.backend.application.payroll


import com.ptmanager.backend.common.access.WorkplaceAccessGuard
import com.ptmanager.backend.common.workedMinutes
import com.ptmanager.backend.domain.AttendanceStatus
import com.ptmanager.backend.domain.Shift
import com.ptmanager.backend.domain.UserRole
import com.ptmanager.backend.application.payroll.LaborCostReport.EmployeeCost
import com.ptmanager.backend.application.payroll.WeeklyPayrollSummary.WeeklyCost
import com.ptmanager.backend.application.port.out.ShiftRepository
import com.ptmanager.backend.application.port.out.UserRepository
import com.ptmanager.backend.application.port.out.WorkplaceRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.util.NoSuchElementException

@Service
class LaborCostService(
    private val workplaceRepository: WorkplaceRepository,
    private val shiftRepository: ShiftRepository,
    private val userRepository: UserRepository,
    private val accessGuard: WorkplaceAccessGuard,
) {

    fun myPayroll(userId: Long, month: YearMonth): MyPayrollSummary {
        val user = userRepository.findById(userId).orElseThrow {
            NoSuchElementException("사용자를 찾을 수 없습니다.")
        }
        val workplaceId = user.workplaceId
            ?: throw IllegalArgumentException("소속 매장이 없습니다.")
        val shifts = payableShifts(workplaceId, month.atDay(1), month.atEndOfMonth())
            .filter { it.employeeId == userId }
        val minutes = shifts.sumOf { actualOrScheduledMinutes(it) }
        return MyPayrollSummary(
            yearMonth = month.toString(),
            hourlyWage = user.hourlyWage,
            workedMinutes = minutes,
            amount = minutes * user.hourlyWage / 60,
            weeks = weeklyCosts(shifts, mapOf(userId to user.hourlyWage)),
        )
    }

    fun calculate(workplaceId: Long, from: LocalDate, to: LocalDate): LaborCostReport {
        val minutesByEmployee = payableShifts(workplaceId, from, to)
            .groupingBy { it.employeeId }
            .fold(0L) { minutes, shift -> minutes + actualOrScheduledMinutes(shift) }
            .toMutableMap()
        // 근무 기록이 없는 직원도 0원으로 목록에 포함한다.
        for (member in userRepository.findByWorkplaceIdAndRole(workplaceId, UserRole.EMPLOYEE)) {
            minutesByEmployee.putIfAbsent(member.id!!, 0L)
        }

        val users = userRepository.findAllById(minutesByEmployee.keys).associateBy { it.id }

        val employees = minutesByEmployee.map { (employeeId, totalMinutes) ->
            val user = users[employeeId]
            val hourlyWage = user?.hourlyWage ?: 0
            val name = user?.name ?: "(unknown)"
            val cost = totalMinutes * hourlyWage / 60
            EmployeeCost(employeeId, name, totalMinutes, hourlyWage, cost)
        }

        return LaborCostReport(workplaceId, from, to, employees.sumOf { it.cost }, employees)
    }

    /**
     * 월을 4주 버킷(1–7, 8–14, 15–21, 22–말일)으로 나눠 주차별 인건비 합계를 낸다.
     * 근무는 한 번만 조회해 일자로 버킷에 분배한다. (calculate를 4번 부르지 않음)
     */
    fun weeklyTotals(workplaceId: Long, month: YearMonth, employeeId: Long? = null): List<WeeklyCost> {
        val shifts = payableShifts(workplaceId, month.atDay(1), month.atEndOfMonth())
            .filter { employeeId == null || it.employeeId == employeeId }
        val wages = userRepository.findAllById(shifts.map { it.employeeId }.distinct())
            .associate { it.id to it.hourlyWage }

        return weeklyCosts(shifts, wages)
    }

    // 월 합계는 합산 후, 주차 합계는 근무별로 원 미만을 버린다.
    private fun weeklyCosts(shifts: List<Shift>, wages: Map<Long?, Int>): List<WeeklyCost> {
        val buckets = LongArray(WEEK_BUCKETS)
        for (shift in shifts) {
            val index = ((shift.workDate.dayOfMonth - 1) / 7).coerceAtMost(WEEK_BUCKETS - 1)
            val wage = wages[shift.employeeId] ?: 0
            buckets[index] += actualOrScheduledMinutes(shift) * wage / 60
        }
        return buckets.mapIndexed { i, amount -> WeeklyCost(week = i + 1, amount = amount) }
    }

    private fun payableShifts(workplaceId: Long, from: LocalDate, to: LocalDate): List<Shift> {
        accessGuard.requireMemberOf(workplaceId)
        if (!workplaceRepository.existsById(workplaceId)) {
            throw NoSuchElementException("Workplace not found.")
        }
        require(!to.isBefore(from)) { "'to' must not be before 'from'." }
        return shiftRepository.findByWorkplaceIdAndWorkDateBetween(workplaceId, from, to)
            .filter { it.attendanceStatus != AttendanceStatus.ABSENT }
    }

    /** 출퇴근 기록이 모두 있으면 실제 근무시간, 없으면 편성 시간으로 계산한다. */
    private fun actualOrScheduledMinutes(shift: Shift): Long {
        val inAt = shift.checkedInAt
        val outAt = shift.checkedOutAt
        return if (inAt != null && outAt != null && outAt.isAfter(inAt)) {
            Duration.between(inAt, outAt).toMinutes()
        } else {
            workedMinutes(shift.startTime, shift.endTime)
        }
    }

    companion object {
        private const val WEEK_BUCKETS = 4
    }
}
