package com.ptmanager.backend.adapter.inbound.web.notice

import com.ptmanager.backend.application.notice.*

import com.ptmanager.backend.common.PageResponse
import com.ptmanager.backend.common.pageRequest
import com.ptmanager.backend.application.port.out.UploadFile
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/notices")
class NoticeController(
    private val noticeService: NoticeService,
) {

    @GetMapping
    fun findNotices(
        @RequestParam workplaceId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<NoticeResponse> =
        PageResponse.of(noticeService.findByWorkplace(workplaceId, pageRequest(page, size)))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLOYER')")
    fun createNotice(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: CreateNoticeRequest,
    ): NoticeResponse = noticeService.create(
        request.workplaceId,
        userId,
        request.title,
        request.body,
        request.attachmentUrls,
    )

    @GetMapping("/{noticeId}")
    fun findNotice(@PathVariable noticeId: Long): NoticeResponse = noticeService.findById(noticeId)

    @DeleteMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('EMPLOYER')")
    fun deleteNotice(@PathVariable noticeId: Long) = noticeService.delete(noticeId)

    @PostMapping("/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLOYER')")
    fun uploadAttachment(@RequestParam("file") file: MultipartFile): NoticeAttachmentResponse =
        file.inputStream.use { input ->
            NoticeAttachmentResponse.from(
                noticeService.uploadAttachment(UploadFile(file.originalFilename, file.contentType, file.size, input)),
            )
        }

    @GetMapping("/unread")
    fun unread(
        @AuthenticationPrincipal userId: Long,
        @RequestParam workplaceId: Long,
    ): UnreadFlag = UnreadFlag(noticeService.hasUnread(workplaceId, userId))

    @PostMapping("/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun read(@AuthenticationPrincipal userId: Long) = noticeService.markRead(userId)
}
