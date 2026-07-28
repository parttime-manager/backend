package com.ptmanager.backend.application.port.out

data class PageQuery(val pageNumber: Int, val pageSize: Int, val newestFirst: Boolean = false) {
    init { require(pageNumber >= 0 && pageSize in 1..100) }
}

data class PageResult<T>(val content: List<T>, val number: Int, val size: Int, val totalElements: Long, val totalPages: Int) {
    fun <R> map(transform: (T) -> R): PageResult<R> = PageResult(content.map(transform), number, size, totalElements, totalPages)
}
