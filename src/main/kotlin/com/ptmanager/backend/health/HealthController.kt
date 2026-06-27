package com.ptmanager.backend.health

import com.ptmanager.backend.health.dto.HealthResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/health")
class HealthController {

    @GetMapping
    fun health(): HealthResponse = HealthResponse("UP", Instant.now())
}
