package com.ptmanager.backend.handover

import com.ptmanager.backend.domain.HandoverCategory
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class CreateHandoverRequest(
    val workplaceId: Long,
    val category: HandoverCategory,
    @field:NotBlank val title: String,
    @field:NotBlank val content: String,
)

/** 인수인계 노트 응답. 작성자명을 포함한다. */
data class HandoverResponse(
    val id: Long?,
    val workplaceId: Long,
    val authorId: Long,
    val authorName: String?,
    val category: HandoverCategory,
    val title: String,
    val content: String,
    val createdAt: Instant?,
)
