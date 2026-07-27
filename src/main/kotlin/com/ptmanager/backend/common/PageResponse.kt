package com.ptmanager.backend.common

import com.ptmanager.backend.application.port.out.PageResult
import com.ptmanager.backend.application.port.out.PageQuery

/** 페이지네이션 공통 응답 래퍼. (Notice/Notification 목록 등) */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    companion object {
        fun <T> of(page: PageResult<T>) =
            PageResponse(page.content, page.number, page.size, page.totalElements, page.totalPages)
    }
}

/** 목록 API 공통 페이지 파라미터 보정. (page ≥ 0, size 1..100) */
fun pageRequest(page: Int, size: Int): PageQuery =
    PageQuery(page.coerceAtLeast(0), size.coerceIn(1, 100))
