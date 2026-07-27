package com.ptmanager.backend

import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CleanArchitectureTest {
    @Test
    fun `dependencies point toward application ports and domain`() {
        val source = File("src/main/kotlin/com/ptmanager/backend")
        assertTrue(source.resolve("application/port/out").isDirectory)
        assertTrue(source.resolve("adapter/out/persistence").isDirectory)
        val violations = source.walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
            val path = file.relativeTo(source).invariantSeparatorsPath
            file.readLines().mapIndexedNotNull { index, line ->
                val inward = path.startsWith("domain/") || path.startsWith("application/")
                val reverse = inward && line.startsWith("import com.ptmanager.backend.adapter.")
                val domain = path.startsWith("domain/") && line.startsWith("import com.ptmanager.backend.application.")
                val persistence = path.startsWith("application/") &&
                    ("org.springframework.data.jpa" in line || "jakarta.persistence" in line)
                val port = path.startsWith("application/port/") && line.startsWith("import org.springframework.")
                val web = path.startsWith("adapter/inbound/") &&
                    (line.startsWith("import com.ptmanager.backend.adapter.out.persistence.") || "@Transactional" in line)
                if (reverse || domain || persistence || port || web) "$path:${index + 1}: $line" else null
            }
        }.toList()
        assertTrue(violations.isEmpty(), violations.joinToString("\n"))
    }
}
