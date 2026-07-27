package com.ptmanager.backend.adapter.out.storage

import com.ptmanager.backend.application.port.out.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * 기본(개발용) 스토리지. 실제 업로드 없이 가짜 URL을 반환한다.
 * `s3.enabled=true` 가 아니면 이 빈이 사용된다. (실업로드는 [S3StorageService])
 */
@Service
@ConditionalOnProperty(prefix = "s3", name = ["enabled"], havingValue = "false", matchIfMissing = true)
class StubStorageService : StorageService {

    override fun store(file: UploadFile): String {
        val key = "${UUID.randomUUID()}-${file.originalFilename ?: "file"}"
        return "https://ptmanager-stub-storage.local/uploads/$key"
    }
}
