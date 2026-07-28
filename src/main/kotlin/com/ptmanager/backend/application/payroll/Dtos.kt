package com.ptmanager.backend.application.payroll


import java.time.LocalDate

data class PayrollItem(
    val employeeId: Long,
    val employeeName: String,
    val hourlyWage: Int,
    val workedMinutes: Long,
    val amount: Long,
)

/** 월 단위 인건비 집계. (API 명세의 PayrollSummary) */
data class PayrollSummary(
    val workplaceId: Long,
    val yearMonth: String,
    val totalAmount: Long,
    val items: List<PayrollItem>,
)

/** 월을 4주 버킷(1–7, 8–14, 15–21, 22–말일)으로 나눈 주차별 인건비 집계. */
data class WeeklyPayrollSummary(
    val workplaceId: Long,
    val yearMonth: String,
    val weeks: List<WeeklyCost>,
) {

    data class WeeklyCost(
        val week: Int,
        val amount: Long,
    )
}

/** 직원 본인의 월 급여 집계. (실근태 기준, 결근 제외) */
data class MyPayrollSummary(
    val yearMonth: String,
    val hourlyWage: Int,
    val workedMinutes: Long,
    val amount: Long,
    val weeks: List<WeeklyPayrollSummary.WeeklyCost> = emptyList(),
)

data class LaborCostReport(
    val workplaceId: Long,
    val from: LocalDate,
    val to: LocalDate,
    val totalCost: Long,
    val employees: List<EmployeeCost>,
) {

    data class EmployeeCost(
        val employeeId: Long,
        val name: String,
        val totalMinutes: Long,
        val hourlyWage: Int,
        val cost: Long,
    )
}
