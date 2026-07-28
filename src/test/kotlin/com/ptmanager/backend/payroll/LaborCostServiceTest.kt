package com.ptmanager.backend.payroll

import com.ptmanager.backend.adapter.inbound.web.payroll.*
import com.ptmanager.backend.application.payroll.*

import com.ptmanager.backend.common.BusinessError
import com.ptmanager.backend.common.BusinessException
import com.ptmanager.backend.common.access.WorkplaceAccessGuard
import com.ptmanager.backend.domain.AttendanceStatus
import com.ptmanager.backend.domain.Shift
import com.ptmanager.backend.domain.User
import com.ptmanager.backend.domain.UserRole
import com.ptmanager.backend.adapter.out.persistence.ShiftRepository
import com.ptmanager.backend.adapter.out.persistence.UserRepository
import com.ptmanager.backend.adapter.out.persistence.WorkplaceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.Instant
import java.time.LocalTime
import java.time.YearMonth
import java.util.Optional

class LaborCostServiceTest {
    private val workplaces = mock(WorkplaceRepository::class.java)
    private val shifts = mock(ShiftRepository::class.java)
    private val users = mock(UserRepository::class.java)
    private val access = mock(WorkplaceAccessGuard::class.java)
    private val service = LaborCostService(workplaces, shifts, users, access)
    private val month = YearMonth.of(2026, 3)

    private fun givenShifts(vararg records: Shift) {
        `when`(workplaces.existsById(1L)).thenReturn(true)
        `when`(shifts.findByWorkplaceIdAndWorkDateBetween(1L, month.atDay(1), month.atEndOfMonth()))
            .thenReturn(records.toList())
        val employees = listOf(User(id = 2L, name = "직원", hourlyWage = 10000), User(id = 3L, name = "미편성", hourlyWage = 12000))
        `when`(users.findByWorkplaceIdAndRole(1L, UserRole.EMPLOYEE)).thenReturn(employees)
        `when`(users.findAllById(anyIterable<Long>())).thenAnswer { invocation ->
            val ids = invocation.getArgument<Iterable<Long>>(0).toSet()
            employees.filter { it.id in ids }
        }
    }

    private fun shift(day: Int, start: String = "09:00", end: String = "09:01") = Shift(
        workplaceId = 1L, employeeId = 2L, workDate = month.atDay(day),
        startTime = LocalTime.parse(start), endTime = LocalTime.parse(end),
    )

    @Test
    fun monthlyReportPreservesEmployeeOrderZeroRowsAndAggregateRounding() {
        givenShifts(shift(1), shift(2), shift(3).apply { employeeId = 99L })

        val report = service.calculate(1L, month.atDay(1), month.atEndOfMonth())

        assertEquals(listOf(2L, 99L, 3L), report.employees.map { it.employeeId })
        assertEquals(listOf(2L, 1L, 0L), report.employees.map { it.totalMinutes })
        assertEquals(listOf(333L, 0L, 0L), report.employees.map { it.cost })
        assertEquals("(unknown)", report.employees[1].name)
        assertEquals(333L, report.totalCost)
    }

    @Test
    fun weeklyReportPreservesBoundariesPerShiftRoundingAndEmployeeFilter() {
        givenShifts(*listOf(1, 7, 8, 14, 15, 21, 22, 28, 29, 31).map { shift(it) }.toTypedArray())

        val weeks = service.weeklyTotals(1L, month)

        assertEquals(listOf(1, 2, 3, 4), weeks.map { it.week })
        assertEquals(listOf(332L, 332L, 332L, 664L), weeks.map { it.amount })
        assertEquals(listOf(0L, 0L, 0L, 0L), service.weeklyTotals(1L, month, 3L).map { it.amount })
    }

    @Test
    fun reportsUseValidAttendanceOtherwiseScheduledMinutesAndExcludeAbsences() {
        val checkIn = Instant.parse("2026-03-01T09:00:00Z")
        givenShifts(
            shift(1).apply { checkedInAt = checkIn; checkedOutAt = checkIn.plusSeconds(90 * 60) },
            shift(2, "22:00", "06:00"),
            shift(3, "09:00", "09:00"),
            shift(4, "09:00", "10:00").apply { checkedInAt = checkIn },
            shift(5, "09:00", "10:00").apply { checkedInAt = checkIn; checkedOutAt = checkIn },
            shift(6, "09:00", "10:00").apply { checkedInAt = checkIn; checkedOutAt = checkIn.minusSeconds(60) },
            shift(7).apply { attendanceStatus = AttendanceStatus.ABSENT },
        )

        val report = service.calculate(1L, month.atDay(1), month.atEndOfMonth())

        assertEquals(2190L, report.employees.first().totalMinutes)
        assertEquals(365000L, report.totalCost)
        assertEquals(listOf(365000L, 0L, 0L, 0L), service.weeklyTotals(1L, month).map { it.amount })
    }

    @Test
    fun myPayrollIncludesOnlyTheCurrentEmployee() {
        givenShifts(shift(1, "09:00", "10:00"), shift(8).apply { employeeId = 3L })
        `when`(users.findById(2L)).thenReturn(Optional.of(User(id = 2L, workplaceId = 1L, hourlyWage = 10000)))

        val report = service.myPayroll(2L, month)

        assertEquals("2026-03", report.yearMonth)
        assertEquals(10000, report.hourlyWage)
        assertEquals(60L, report.workedMinutes)
        assertEquals(10000L, report.amount)
        assertEquals(listOf(10000L, 0L, 0L, 0L), report.weeks.map { it.amount })
        verify(shifts, times(1)).findByWorkplaceIdAndWorkDateBetween(1L, month.atDay(1), month.atEndOfMonth())
        verify(users, never()).findAllById(anyIterable<Long>())
    }

    @Test
    fun myPayrollKeepsMonthlyAggregateAndWeeklyPerShiftRounding() {
        givenShifts(shift(1), shift(2))
        `when`(users.findById(2L)).thenReturn(Optional.of(User(id = 2L, workplaceId = 1L, hourlyWage = 10000)))

        val report = service.myPayroll(2L, month)

        assertEquals(333L, report.amount)
        assertEquals(listOf(332L, 0L, 0L, 0L), report.weeks.map { it.amount })
    }

    @Test
    fun myPayrollWithoutShiftsUsesCurrentWageAndZeroTotals() {
        givenShifts()
        `when`(users.findById(3L)).thenReturn(Optional.of(User(id = 3L, workplaceId = 1L, hourlyWage = 12000)))

        val report = service.myPayroll(3L, month)

        assertEquals(12000, report.hourlyWage)
        assertEquals(0L, report.workedMinutes)
        assertEquals(0L, report.amount)
        assertEquals(listOf(0L, 0L, 0L, 0L), report.weeks.map { it.amount })
    }

    @Test
    fun myPayrollRejectsMissingUserOrWorkplaceBeforeQueryingShifts() {
        `when`(users.findById(2L)).thenReturn(Optional.empty())
        assertThrows<NoSuchElementException> { service.myPayroll(2L, month) }
        `when`(users.findById(2L)).thenReturn(Optional.of(User(id = 2L)))
        assertThrows<IllegalArgumentException> { service.myPayroll(2L, month) }
        verifyNoInteractions(workplaces, shifts)
    }

    @Test
    fun bothReportsCheckMembershipBeforeQueryingWorkplaceOrShifts() {
        doThrow(BusinessException(BusinessError.ACCESS_DENIED, "접근 거부")).`when`(access).requireMemberOf(1L)

        assertThrows<BusinessException> { service.calculate(1L, month.atDay(1), month.atEndOfMonth()) }
        assertThrows<BusinessException> { service.weeklyTotals(1L, month) }
        verifyNoInteractions(workplaces, shifts, users)
    }

    @Test
    fun missingWorkplaceAndReversedRangeFailBeforeQueryingShifts() {
        assertThrows<NoSuchElementException> { service.calculate(1L, month.atDay(1), month.atEndOfMonth()) }
        assertThrows<NoSuchElementException> { service.weeklyTotals(1L, month) }
        `when`(workplaces.existsById(1L)).thenReturn(true)
        assertThrows<IllegalArgumentException> { service.calculate(1L, month.atEndOfMonth(), month.atDay(1)) }
        verifyNoInteractions(shifts, users)
    }
}
