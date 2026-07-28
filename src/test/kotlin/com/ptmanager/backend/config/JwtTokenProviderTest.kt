package com.ptmanager.backend.config

import com.ptmanager.backend.configuration.*

import com.ptmanager.backend.adapter.out.security.JwtTokenProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JwtTokenProviderTest {
    private val provider = JwtTokenProvider("a-secret-that-is-long-enough-for-hs256", 60_000, 120_000)

    @Test
    fun oneVerifiedPayloadProvidesTheSubjectAndRole() {
        val claims = provider.parseVerified(provider.createAccessToken(42L, "EMPLOYEE"))!!
        assertEquals("42", claims.subject)
        assertEquals("EMPLOYEE", claims["role"])
        val refresh = provider.parseVerified(provider.createRefreshToken(42L))!!
        assertEquals("42", refresh.subject)
        assertNull(refresh["role"])
    }

    @Test
    fun invalidExpiredAndWronglySignedTokensRemainUnauthenticated() {
        assertNull(provider.parseVerified("broken"))
        assertNull(provider.parseVerified(""))
        val other = JwtTokenProvider("another-secret-that-is-long-enough-for-hs256", 60_000, 120_000)
        assertNull(provider.parseVerified(other.createAccessToken(42L, "EMPLOYEE")))
        val expired = JwtTokenProvider("a-secret-that-is-long-enough-for-hs256", -60_000, -60_000)
        assertNull(provider.parseVerified(expired.createAccessToken(42L, "EMPLOYEE")))
    }
}
