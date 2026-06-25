package com.ptmanager.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ptmanager.backend.domain.Shift;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByEmployeeIdOrderByWorkDateAscStartTimeAsc(Long employeeId);

    List<Shift> findByWorkplaceIdAndWorkDateBetween(Long workplaceId, LocalDate from, LocalDate to);
}
