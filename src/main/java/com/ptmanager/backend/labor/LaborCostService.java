package com.ptmanager.backend.labor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.ptmanager.backend.domain.Shift;
import com.ptmanager.backend.domain.User;
import com.ptmanager.backend.labor.LaborCostReport.EmployeeCost;
import com.ptmanager.backend.repository.ShiftRepository;
import com.ptmanager.backend.repository.UserRepository;
import com.ptmanager.backend.repository.WorkplaceRepository;

@Service
public class LaborCostService {

    private final WorkplaceRepository workplaceRepository;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    public LaborCostService(WorkplaceRepository workplaceRepository,
                            ShiftRepository shiftRepository,
                            UserRepository userRepository) {
        this.workplaceRepository = workplaceRepository;
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    public LaborCostReport calculate(long workplaceId, LocalDate from, LocalDate to) {
        if (!workplaceRepository.existsById(workplaceId)) {
            throw new NoSuchElementException("Workplace not found.");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' must not be before 'from'.");
        }

        List<Shift> shifts = shiftRepository.findByWorkplaceIdAndWorkDateBetween(workplaceId, from, to);

        Map<Long, Long> minutesByEmployee = new LinkedHashMap<>();
        for (Shift shift : shifts) {
            long minutes = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
            minutesByEmployee.merge(shift.getEmployeeId(), minutes, Long::sum);
        }

        List<EmployeeCost> employees = new ArrayList<>();
        long totalCost = 0L;
        for (Map.Entry<Long, Long> entry : minutesByEmployee.entrySet()) {
            long employeeId = entry.getKey();
            long totalMinutes = entry.getValue();
            User user = userRepository.findById(employeeId).orElse(null);
            int hourlyWage = user == null ? 0 : user.getHourlyWage();
            String name = user == null ? "(unknown)" : user.getName();
            long cost = totalMinutes * hourlyWage / 60;
            totalCost += cost;
            employees.add(new EmployeeCost(employeeId, name, totalMinutes, hourlyWage, cost));
        }

        return new LaborCostReport(workplaceId, from, to, totalCost, employees);
    }
}
