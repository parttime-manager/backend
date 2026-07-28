package com.ptmanager.backend.application.notice


import com.ptmanager.backend.domain.NoticeAttachment
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class CreateNoticeRequest(
    val workplaceId: Long,
    @field:NotBlank val title: String,
    @field:NotBlank val body: String,
    val attachmentUrls: List<String> = emptyList(),
)

/** 공지 응답. 명세 Notice 스키마대로 작성자명·첨부 목록을 포함한다. */
data class NoticeResponse(
    val id: Long?,
    val workplaceId: Long,
    val authorId: Long,
    val authorName: String?,
    val title: String,
    val body: String,
    val attachments: List<NoticeAttachmentResponse>,
    val createdAt: Instant?,
)

/** 미확인 공지 존재 여부 (레드 닷). */
data class UnreadFlag(
    val hasUnread: Boolean,
)

data class NoticeAttachmentResponse(
    val id: Long?,
    val noticeId: Long,
    val fileUrl: String,
    val createdAt: Instant?,
) {
    companion object {
        fun from(entity: NoticeAttachment): NoticeAttachmentResponse = NoticeAttachmentResponse(
            id = entity.id,
            noticeId = entity.noticeId,
            fileUrl = entity.fileUrl,
            createdAt = entity.createdAt,
        )
    }
}
