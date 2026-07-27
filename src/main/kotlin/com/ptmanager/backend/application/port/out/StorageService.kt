package com.ptmanager.backend.application.port.out

import com.ptmanager.backend.application.port.out.*
import java.io.InputStream

data class UploadFile(
    val originalFilename: String?,
    val contentType: String?,
    val size: Long,
    val inputStream: InputStream,
)

/** 파일 저장 추상화. 호출자가 스트림을 닫는다. */
interface StorageService {
    fun store(file: UploadFile): String
}
