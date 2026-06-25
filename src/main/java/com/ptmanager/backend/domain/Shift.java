package com.ptmanager.backend.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shift")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workplace_id", nullable = false)
    private Long workplaceId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "checked_in", nullable = false)
    private boolean checkedIn;

    protected Shift() {
    }

    public Shift(Long id, Long workplaceId, Long employeeId, LocalDate workDate,
                 LocalTime startTime, LocalTime endTime, boolean checkedIn) {
        this.id = id;
        this.workplaceId = workplaceId;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.checkedIn = checkedIn;
    }

    public Long getId() {
        return id;
    }

    public Long getWorkplaceId() {
        return workplaceId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}
