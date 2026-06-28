package com.ptmanager.backend.notice.dto

import com.ptmanager.backend.domain.NoticeAttachment
import java.time.Instant

/** 공지 응답. 명세 Notice 스키마대로 작성자명·첨부 목록을 포함한다. */
data class NoticeResponse(
    val id: Long?,
    val workplaceId: Long,
    val authorId: Long,
    val authorName: String?,
    val title: String,
    val body: String,
    val attachments: List<NoticeAttachment>,
    val createdAt: Instant?,
)
