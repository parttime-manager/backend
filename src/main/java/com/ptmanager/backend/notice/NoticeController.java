package com.ptmanager.backend.notice;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ptmanager.backend.domain.Notice;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public List<Notice> findNotices(@RequestParam long workplaceId) {
        return noticeService.findByWorkplace(workplaceId);
    }

    @GetMapping("/{id}")
    public Notice findNotice(@PathVariable long id) {
        return noticeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Notice createNotice(@Valid @RequestBody CreateNoticeRequest request) {
        return noticeService.create(request.workplaceId(), request.authorId(), request.title(), request.body());
    }

    public record CreateNoticeRequest(
        @NotNull Long workplaceId,
        @NotNull Long authorId,
        @NotBlank String title,
        @NotBlank String body
    ) {
    }
}
