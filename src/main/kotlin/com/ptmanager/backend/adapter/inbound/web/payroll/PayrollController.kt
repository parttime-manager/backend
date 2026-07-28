package com.ptmanager.backend.adapter.inbound.web.payroll

import com.ptmanager.backend.application.payroll.*

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.YearMonth
import java.time.format.DateTimeParseException

@RestController
@RequestMapping("/api/payroll")
class PayrollController(
    private val laborCostService: LaborCostService,
) {

    /** 로그인한 직원 본인의 월 급여. yearMonth 예: 2026-06 */
    @GetMapping("/me")
    fun myPayroll(
        @AuthenticationPrincipal userId: Long,
        @RequestParam yearMonth: String,
    ): MyPayrollSummary {
        val ym = parseYearMonth(yearMonth)
        return laborCostService.myPayroll(userId, ym)
    }

    /** 월 단위 인건비 집계. yearMonth 예: 2026-06 */
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    fun payroll(
        @RequestParam workplaceId: Long,
        @RequestParam yearMonth: String,
    ): PayrollSummary {
        val ym = parseYearMonth(yearMonth)
        val report = laborCostService.calculate(workplaceId, ym.atDay(1), ym.atEndOfMonth())
        val items = report.employees.map {
            PayrollItem(
                employeeId = it.employeeId,
                employeeName = it.name,
                hourlyWage = it.hourlyWage,
                workedMinutes = it.totalMinutes,
                amount = it.cost,
            )
        }
        return PayrollSummary(workplaceId, yearMonth, report.totalCost, items)
    }

    /** 월 인건비의 주차별(1–7, 8–14, 15–21, 22–말일) 추이. yearMonth 예: 2026-06 */
    @GetMapping("/weekly")
    @PreAuthorize("hasRole('EMPLOYER')")
    fun weekly(
        @RequestParam workplaceId: Long,
        @RequestParam yearMonth: String,
    ): WeeklyPayrollSummary {
        val ym = parseYearMonth(yearMonth)
        return WeeklyPayrollSummary(workplaceId, yearMonth, laborCostService.weeklyTotals(workplaceId, ym))
    }

    private fun parseYearMonth(yearMonth: String): YearMonth = try {
        YearMonth.parse(yearMonth)
    } catch (ex: DateTimeParseException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "yearMonth 형식이 올바르지 않습니다 (YYYY-MM).")
    }
}
