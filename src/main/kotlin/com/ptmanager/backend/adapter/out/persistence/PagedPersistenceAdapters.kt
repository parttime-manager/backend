package com.ptmanager.backend.adapter.out.persistence

import com.ptmanager.backend.domain.*
import com.ptmanager.backend.application.port.out.PageQuery
import com.ptmanager.backend.application.port.out.PageResult
import com.ptmanager.backend.application.port.out.EntityStore
import org.springframework.stereotype.Component
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.*
import java.util.Optional

@Component
class NoticePersistenceAdapter(private val repository: NoticeRepository) :
    com.ptmanager.backend.application.port.out.NoticeRepository, EntityStore<Notice, Long> by repository {
    override fun findByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long, pageable: PageQuery): PageResult<Notice> =
        repository.findByWorkplaceIdOrderByCreatedAtDesc(workplaceId, pageable.asPageable()).asResult()
    override fun findFirstByWorkplaceIdOrderByCreatedAtDesc(workplaceId: Long): Notice? =
        repository.findFirstByWorkplaceIdOrderByCreatedAtDesc(workplaceId)
}

@Component
class NotificationPersistenceAdapter(private val repository: NotificationRepository) :
    com.ptmanager.backend.application.port.out.NotificationRepository, EntityStore<Notification, Long> by repository {
    override fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: PageQuery): PageResult<Notification> =
        repository.findByUserIdOrderByCreatedAtDesc(userId, pageable.asPageable()).asResult()
    override fun findByUserIdAndReadOrderByCreatedAtDesc(userId: Long, read: Boolean, pageable: PageQuery): PageResult<Notification> =
        repository.findByUserIdAndReadOrderByCreatedAtDesc(userId, read, pageable.asPageable()).asResult()
    override fun findByUserIdAndRead(userId: Long, read: Boolean): List<Notification> =
        repository.findByUserIdAndRead(userId, read)
    override fun countByUserIdAndReadFalse(userId: Long): Long =
        repository.countByUserIdAndReadFalse(userId)
}

private fun PageQuery.asPageable() = PageRequest.of(pageNumber, pageSize, if (newestFirst) Sort.by(Sort.Direction.DESC, "createdAt") else Sort.unsorted())
private fun <T> org.springframework.data.domain.Page<T>.asResult() = PageResult(content, number, size, totalElements, totalPages)
