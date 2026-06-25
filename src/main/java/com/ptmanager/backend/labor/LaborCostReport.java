package com.ptmanager.backend.labor;

import java.time.LocalDate;
import java.util.List;

public record LaborCostReport(
    long workplaceId,
    LocalDate from,
    LocalDate to,
    long totalCost,
    List<EmployeeCost> employees
) {

    public record EmployeeCost(
        long employeeId,
        String name,
        long totalMinutes,
        int hourlyWage,
        long cost
    ) {
    }
}
