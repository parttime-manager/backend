package com.ptmanager.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ptmanager.backend.domain.Workplace;

public interface WorkplaceRepository extends JpaRepository<Workplace, Long> {

    Optional<Workplace> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}
