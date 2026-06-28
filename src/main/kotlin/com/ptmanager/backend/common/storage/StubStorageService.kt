package com.ptmanager.backend.common.storage

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * 개발용 스텁 스토리지. 실제 업로드 없이 가짜 URL을 반환한다.
 * TODO: 운영용 S3StorageService(AWS SDK + 버킷/자격증명)로 교체.
 */
@Service
class StubStorageService : StorageService {

    override fun store(file: MultipartFile): String {
        val key = "${UUID.randomUUID()}-${file.originalFilename ?: "file"}"
        return "https://ptmanager-stub-storage.local/uploads/$key"
    }
}
