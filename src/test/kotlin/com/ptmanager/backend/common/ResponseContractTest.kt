package com.ptmanager.backend.common


import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ptmanager.backend.domain.*
import com.ptmanager.backend.adapter.inbound.web.user.*
import com.ptmanager.backend.application.user.*
import com.ptmanager.backend.application.workplace.WorkplaceResponse
import com.ptmanager.backend.application.swaprequest.SwapRequestResponse
import com.ptmanager.backend.application.notification.NotificationResponse
import com.ptmanager.backend.application.notice.NoticeAttachmentResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

class ResponseContractTest {
    private val mapper = jacksonObjectMapper().findAndRegisterModules()

    @Test
    fun responseDtosPreserveExistingJsonIncludingNullsTimestampsAndIsRead() {
        val now = Instant.parse("2026-10-20T00:00:00Z")
        val user = User(id = 7L, email = "test@example.com", password = "secret", name = "직원",
            workplaceId = 2L, hourlyWage = 12000, lastReadNoticeAt = now, createdAt = now, updatedAt = now)
        val workplace = Workplace(id = 2L, name = "매장", address = null, inviteCode = "ABC", createdAt = now)
        val swap = SwapRequest(id = 3L, workplaceId = 2L, shiftId = 4L, requesterId = 7L,
            substituteId = 8L, reason = "사유", status = SwapRequestStatus.APPROVED, createdAt = now)
        val notification = Notification(id = 9L, userId = 7L, message = "알림", read = true, createdAt = now)
        val device = DeviceToken(id = 5L, userId = 7L, token = "token", updatedAt = now)
        val setting = NotificationSetting(userId = 7L, noticeEnabled = false)
        val attachment = NoticeAttachment(id = 6L, noticeId = 10L, fileUrl = "https://example.com/file", createdAt = now)
        val pairs = listOf(
            user to UserResponse.from(user), workplace to WorkplaceResponse.from(workplace),
            swap to SwapRequestResponse.from(swap), notification to NotificationResponse.from(notification),
            device to DeviceTokenResponse.from(device), setting to NotificationSettingResponse.from(setting),
            attachment to NoticeAttachmentResponse.from(attachment),
        )
        for ((entity, response) in pairs) {
            assertEquals(mapper.valueToTree<JsonNode>(entity), mapper.valueToTree<JsonNode>(response))
        }
        assertFalse(mapper.valueToTree<JsonNode>(UserResponse.from(user)).has("password"))
        val inboxJson = mapper.valueToTree<JsonNode>(NotificationResponse.from(notification))
        assertTrue(inboxJson["isRead"].asBoolean())
        assertFalse(inboxJson.has("read"))
    }

    @Test
    fun applicationErrorsKeepTheirHttpStatusesAndErrorCodes() {
        val statuses = mapOf(
            BusinessError.INVALID_INPUT to (400 to "BAD_REQUEST"),
            BusinessError.AUTHENTICATION_FAILED to (401 to "UNAUTHORIZED"),
            BusinessError.ACCESS_DENIED to (403 to "FORBIDDEN"),
            BusinessError.STATE_CONFLICT to (409 to "CONFLICT"),
        )
        for ((error, expected) in statuses) {
            val response = ApiExceptionHandler().handleBusiness(BusinessException(error, "reason"))
            assertEquals(expected.first, response.statusCode.value())
            assertEquals(expected.second, response.body!!.code)
            assertEquals("reason", response.body!!.message)
            if (expected.first == 401) assertEquals("Bearer", response.headers.getFirst("WWW-Authenticate"))
        }
    }

    @Test
    fun methodErrorsKeepTheAllowHeader() {
        val response = ApiExceptionHandler().handleResponseStatus(
            org.springframework.web.server.MethodNotAllowedException("DELETE", listOf(org.springframework.http.HttpMethod.GET)),
        )
        assertEquals(405, response.statusCode.value())
        assertTrue(response.headers.allow.contains(org.springframework.http.HttpMethod.GET))
    }
}
