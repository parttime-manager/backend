package com.ptmanager.backend.domain;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "swap_request")
public class SwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workplace_id", nullable = false)
    private Long workplaceId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "substitute_id")
    private Long substituteId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SwapRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SwapRequest() {
    }

    public SwapRequest(Long id, Long workplaceId, Long requesterId, Long substituteId,
                       LocalDate workDate, String reason, SwapRequestStatus status, Instant createdAt) {
        this.id = id;
        this.workplaceId = workplaceId;
        this.requesterId = requesterId;
        this.substituteId = substituteId;
        this.workDate = workDate;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getWorkplaceId() {
        return workplaceId;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public Long getSubstituteId() {
        return substituteId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public String getReason() {
        return reason;
    }

    public SwapRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SwapRequestStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
