package com.ptmanager.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ptmanager.backend.domain.SwapRequest;

public interface SwapRequestRepository extends JpaRepository<SwapRequest, Long> {

    List<SwapRequest> findByWorkplaceIdOrderByCreatedAtDesc(Long workplaceId);

    List<SwapRequest> findAllByOrderByCreatedAtDesc();
}
