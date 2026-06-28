package com.ptmanager.backend.payroll

import com.ptmanager.backend.common.access.WorkplaceAccessGuard
import com.ptmanager.backend.domain.AttendanceStatus
import com.ptmanager.backend.payroll.dto.LaborCostReport
import com.ptmanager.backend.payroll.dto.LaborCostReport.EmployeeCost
import com.ptmanager.backend.repository.ShiftRepository
import com.ptmanager.backend.repository.UserRepository
import com.ptmanager.backend.repository.WorkplaceRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.util.NoSuchElementException

@Service
class LaborCostService(
    private val workplaceRepository: WorkplaceRepository,
    private val shiftRepository: ShiftRepository,
    private val userRepository: UserRepository,
    private val accessGuard: WorkplaceAccessGuard,
) {

    fun calculate(workplaceId: Long, from: LocalDate, to: LocalDate): LaborCostReport {
        accessGuard.requireMemberOf(workplaceId)
        if (!workplaceRepository.existsById(workplaceId)) {
            throw NoSuchElementException("Workplace not found.")
        }
        require(!to.isBefore(from)) { "'to' must not be before 'from'." }

        val shifts = shiftRepository.findByWorkplaceIdAndWorkDateBetween(workplaceId, from, to)
            .filter { it.attendanceStatus != AttendanceStatus.ABSENT } // 결근은 집계 제외

        val minutesByEmployee = LinkedHashMap<Long, Long>()
        for (shift in shifts) {
            val minutes = workedMinutes(shift.startTime, shift.endTime)
            minutesByEmployee.merge(shift.employeeId, minutes) { a, b -> a + b }
        }

        val employees = ArrayList<EmployeeCost>()
        var totalCost = 0L
        for ((employeeId, totalMinutes) in minutesByEmployee) {
            val user = userRepository.findById(employeeId).orElse(null)
            val hourlyWage = user?.hourlyWage ?: 0
            val name = user?.name ?: "(unknown)"
            val cost = totalMinutes * hourlyWage / 60
            totalCost += cost
            employees.add(EmployeeCost(employeeId, name, totalMinutes, hourlyWage, cost))
        }

        return LaborCostReport(workplaceId, from, to, totalCost, employees)
    }

    /** 근무 시간(분). 야간 교대(end ≤ start)는 익일로 보정해 양수로 계산한다. */
    private fun workedMinutes(start: LocalTime, end: LocalTime): Long {
        val startMin = (start.toSecondOfDay() / 60).toLong()
        var endMin = (end.toSecondOfDay() / 60).toLong()
        if (endMin <= startMin) endMin += 24 * 60
        return endMin - startMin
    }
}
