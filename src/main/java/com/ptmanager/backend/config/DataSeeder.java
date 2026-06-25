package com.ptmanager.backend.config;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ptmanager.backend.domain.Shift;
import com.ptmanager.backend.domain.User;
import com.ptmanager.backend.domain.UserRole;
import com.ptmanager.backend.domain.Workplace;
import com.ptmanager.backend.repository.ShiftRepository;
import com.ptmanager.backend.repository.UserRepository;
import com.ptmanager.backend.repository.WorkplaceRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final WorkplaceRepository workplaceRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;

    public DataSeeder(WorkplaceRepository workplaceRepository,
                      UserRepository userRepository,
                      ShiftRepository shiftRepository) {
        this.workplaceRepository = workplaceRepository;
        this.userRepository = userRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        Workplace workplace = workplaceRepository.save(
            new Workplace(null, "PT Manager Cafe", "Seoul Gangnam-gu", "CAFE01"));

        User employee = userRepository.save(
            new User(null, "employee@ptmanager.test", "Kim Employee",
                UserRole.EMPLOYEE, workplace.getId(), 12000));
        userRepository.save(
            new User(null, "employer@ptmanager.test", "Park Employer",
                UserRole.EMPLOYER, workplace.getId(), 0));

        LocalDate today = LocalDate.now();
        shiftRepository.save(new Shift(null, workplace.getId(), employee.getId(),
            today, LocalTime.of(9, 0), LocalTime.of(14, 0), false));
        shiftRepository.save(new Shift(null, workplace.getId(), employee.getId(),
            today.plusDays(1), LocalTime.of(14, 0), LocalTime.of(20, 0), false));
    }
}
