package com.ptmanager.backend.notice;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptmanager.backend.domain.Notice;
import com.ptmanager.backend.repository.NoticeRepository;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<Notice> findByWorkplace(long workplaceId) {
        return noticeRepository.findByWorkplaceIdOrderByCreatedAtDesc(workplaceId);
    }

    public Notice findById(long id) {
        return noticeRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Notice not found."));
    }

    @Transactional
    public Notice create(long workplaceId, long authorId, String title, String body) {
        Notice notice = new Notice(null, workplaceId, authorId, title, body, Instant.now());
        return noticeRepository.save(notice);
    }
}
