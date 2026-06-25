package com.ptmanager.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ptmanager.backend.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByWorkplaceIdOrderByCreatedAtDesc(Long workplaceId);
}
