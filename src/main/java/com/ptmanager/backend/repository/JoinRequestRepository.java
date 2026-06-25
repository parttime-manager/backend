package com.ptmanager.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ptmanager.backend.domain.JoinRequest;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    List<JoinRequest> findByWorkplaceIdOrderByCreatedAtDesc(Long workplaceId);
}
