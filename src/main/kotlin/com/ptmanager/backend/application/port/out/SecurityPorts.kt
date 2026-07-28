package com.ptmanager.backend.application.port.out

interface TokenIssuer {
    val accessTokenExpirySeconds: Long
    fun createAccessToken(userId: Long, role: String): String
    fun createRefreshToken(userId: Long): String
    fun verifiedUserId(token: String): Long?
}
fun interface CurrentUser { fun userId(): Long }
