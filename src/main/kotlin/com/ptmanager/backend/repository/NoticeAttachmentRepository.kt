package com.ptmanager.backend.repository

import com.ptmanager.backend.domain.NoticeAttachment
import org.springframework.data.jpa.repository.JpaRepository

interface NoticeAttachmentRepository : JpaRepository<NoticeAttachment, Long> {

    fun findByNoticeId(noticeId: Long): List<NoticeAttachment>

    fun findByNoticeIdIn(noticeIds: Collection<Long>): List<NoticeAttachment>

    fun deleteByNoticeId(noticeId: Long)
}
