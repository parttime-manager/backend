package com.ptmanager.backend.common.storage

import org.springframework.web.multipart.MultipartFile

/** 파일 저장 추상화. 운영에서는 S3 구현으로 교체한다. */
interface StorageService {

    /** 파일을 저장하고 접근 가능한 URL을 반환한다. */
    fun store(file: MultipartFile): String
}
