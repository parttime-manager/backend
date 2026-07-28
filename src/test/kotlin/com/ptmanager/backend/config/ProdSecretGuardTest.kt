package com.ptmanager.backend.config

import com.ptmanager.backend.configuration.*

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ProdSecretGuardTest {

    private val goodJwt = "a".repeat(32)
    private val goodQr = "b".repeat(32)

    @Test
    fun devDefaultsBlockProdStartup() {
        val message = assertThrows<IllegalStateException> {
            ProdSecretGuard("dev-only-secret-please-override-in-production-0123456789abcdef", goodQr).verify()
        }.message
        assertTrue(message!!.contains("JWT_SECRET"))

        assertThrows<IllegalStateException> {
            ProdSecretGuard(goodJwt, "dev-only-qr-secret-please-override-in-production").verify()
        }
    }

    @Test
    fun shortOrReusedSecretsAreRejected() {
        assertThrows<IllegalStateException> { ProdSecretGuard("a".repeat(31), goodQr).verify() }
        assertThrows<IllegalStateException> { ProdSecretGuard(goodJwt, goodJwt).verify() }
    }

    @Test
    fun realSecretsPass() {
        assertDoesNotThrow { ProdSecretGuard(goodJwt, goodQr).verify() }
    }
}
