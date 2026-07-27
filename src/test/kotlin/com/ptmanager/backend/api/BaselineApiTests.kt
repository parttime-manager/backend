package com.ptmanager.backend.api

import com.jayway.jsonpath.JsonPath
import com.ptmanager.backend.application.shift.QrCodeService
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
class BaselineApiTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var qrCodeService: QrCodeService

    @Test
    fun qrIssuanceRequiresPostAndSafeRequestsDoNotRotateTheToken() {
        val (token, _) = signupReturning("qr-http@ptmanager.test", "QR owner", "EMPLOYER")
        val (workplaceId, _) = createWorkplace(token, "QR HTTP contract")
        val previous = qrCodeService.issue(workplaceId, Instant.now().epochSecond - 1)
        val path = "/api/workplaces/$workplaceId/qr-token"
        for (request in listOf(get(path), org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head(path))) {
            mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
                .andExpect(status().isMethodNotAllowed)
            qrCodeService.verify(workplaceId, previous)
        }
        mockMvc.perform(post(path).header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
            .andExpect(status().isOk).andExpect(jsonPath("$.qrToken").isNotEmpty)
        assertThrows<IllegalArgumentException> { qrCodeService.verify(workplaceId, previous) }
    }

    private fun signupReturning(email: String, name: String, role: String): Pair<String, Long> {
        val body = mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","password":"password1","name":"$name","role":"$role"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val token: String = JsonPath.read(body, "$.accessToken")
        val id: Int = JsonPath.read(body, "$.user.id")
        return token to id.toLong()
    }

    private fun createWorkplace(token: String, name: String): Pair<Long, String> {
        val body = mockMvc.perform(
            post("/api/workplaces")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"$name"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val id: Int = JsonPath.read(body, "$.id")
        val code: String = JsonPath.read(body, "$.inviteCode")
        return id.toLong() to code
    }

    private fun joinAndApprove(employeeToken: String, inviteCode: String, employerToken: String) {
        val jr = mockMvc.perform(
            post("/api/join-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $employeeToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"inviteCode":"$inviteCode"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val joinId: Int = JsonPath.read(jr, "$.id")
        mockMvc.perform(
            patch("/api/join-requests/$joinId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $employerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"decision":"APPROVE"}"""),
        ).andExpect(status().isOk)
    }

    private fun createShift(employerToken: String, workplaceId: Long, employeeId: Long, start: String, end: String): Long {
        val body = mockMvc.perform(
            post("/api/shifts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $employerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"workplaceId":$workplaceId,"employeeId":$employeeId,"workDate":"2026-07-01","startTime":"$start","endTime":"$end"}""",
                ),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val id: Int = JsonPath.read(body, "$.id")
        return id.toLong()
    }

    private fun createSwap(token: String, shiftId: Long): Long {
        val body = mockMvc.perform(
            post("/api/swap-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"shiftId":$shiftId,"reason":"테스트"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val id: Int = JsonPath.read(body, "$.id")
        return id.toLong()
    }

    private fun approve(employerToken: String, swapRequestId: Long, applicantId: Long) =
        mockMvc.perform(
            post("/api/swap-requests/$swapRequestId/approve")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $employerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"applicantId":$applicantId}"""),
        )

    private fun loginAs(email: String): String {
        val response = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","password":"password"}"""),
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
        return JsonPath.read(response, "$.accessToken")
    }

    @Test
    fun healthIsPublicAndIncludesDb() {
        // 무인증 공개 + DB 인디케이터 포함(H2 다운이면 DOWN). 상세는 숨긴다.
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", `is`("UP")))
            .andExpect(jsonPath("$.components").doesNotExist())
    }

    @Test
    fun openApiDocIsServed() {
        mockMvc.perform(get("/docs"))
            .andExpect(status().isOk)
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl("/docs.html"))

        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.info.title", `is`("PTManager API")))
            // 전역 커스터마이저: ApiError 스키마 + 공통 에러 응답이 문서에 들어갔는지
            .andExpect(jsonPath("$.components.schemas.ApiError").exists())
            .andExpect(jsonPath("$.paths['/api/auth/me'].get.responses['401']").exists())
    }

    @Test
    fun loginReturnsTokensAndSeedEmployee() {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"employee@ptmanager.test","password":"password"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.user.role", `is`("EMPLOYEE")))
            .andExpect(jsonPath("$.user.password").doesNotExist())
    }

    @Test
    fun loginWithWrongPasswordReturnsUnauthorized() {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"employee@ptmanager.test","password":"wrong"}"""),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun meReturnsCurrentUser() {
        val token = loginAs("employee@ptmanager.test")
        mockMvc.perform(
            get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email", `is`("employee@ptmanager.test")))
    }

    @Test
    fun protectedEndpointWithoutTokenIsUnauthorized() {
        mockMvc.perform(get("/api/swap-requests"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun employeeForbiddenFromEmployerEndpoint() {
        val token = loginAs("employee@ptmanager.test")
        mockMvc.perform(
            post("/api/shifts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "workplaceId": 1,
                      "employeeId": 1,
                      "workDate": "2026-07-01",
                      "startTime": "09:00:00",
                      "endTime": "14:00:00"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun cannotAccessOtherWorkplaceData() {
        val token = loginAs("employee@ptmanager.test") // 소속 매장 = 1
        mockMvc.perform(
            get("/api/notices?workplaceId=999").header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun canAccessOwnWorkplaceData() {
        val token = loginAs("employee@ptmanager.test")
        mockMvc.perform(
            get("/api/notices?workplaceId=1").header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
        )
            .andExpect(status().isOk)
    }

    @Test
    fun employerCanCreateWorkplace() {
        // 시드 사장(매장1)을 옮기지 않도록 새 사장으로 생성한다.
        val signup = mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"createboss@ptmanager.test","password":"password1","name":"생성사장","role":"EMPLOYER"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val token: String = JsonPath.read(signup, "$.accessToken")
        mockMvc.perform(
            post("/api/workplaces")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"테스트 매장","address":"서울"}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.inviteCode").exists())
    }

    @Test
    fun employerCanSetMemberWage() {
        val token = loginAs("employer@ptmanager.test") // 매장1 사장
        mockMvc.perform(
            patch("/api/workplaces/1/members/1/wage")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"hourlyWage":11000}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hourlyWage", `is`(11000)))
            .andExpect(jsonPath("$.password").doesNotExist())
    }

    @Test
    fun employerCanRemoveMember() {
        // 시드 데이터를 건드리지 않도록 일회용 사장/매장/직원을 새로 만든다.
        val (employerToken, _) = signupReturning("removeboss@ptmanager.test", "내보내기사장", "EMPLOYER")
        val (workplaceId, inviteCode) = createWorkplace(employerToken, "내보내기 매장")
        val (employeeToken, employeeId) = signupReturning("removee@ptmanager.test", "내보낼직원", "EMPLOYEE")
        joinAndApprove(employeeToken, inviteCode, employerToken)

        mockMvc.perform(
            delete("/api/workplaces/$workplaceId/members/$employeeId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $employerToken"),
        )
            .andExpect(status().isNoContent)

        // 소속이 해제돼 더 이상 매장 멤버 목록에 없어야 한다.
        val members = mockMvc.perform(
            get("/api/workplaces/$workplaceId/members")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $employerToken"),
        ).andExpect(status().isOk).andReturn().response.contentAsString
        assert(!members.contains("removee@ptmanager.test")) { "내보낸 직원이 멤버 목록에 남아있다: $members" }
    }

    @Test
    fun employeeCannotSetWage() {
        val token = loginAs("employee@ptmanager.test")
        mockMvc.perform(
            patch("/api/workplaces/1/members/1/wage")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"hourlyWage":11000}"""),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun weeklyPayrollBucketsByWeekOfMonth() {
        // 시드/타 테스트와 격리된 신규 매장·직원으로 검증 (실행 순서·시드 시각에 의존하지 않음)
        val (bossToken, _) = signupReturning("weeklyboss@ptmanager.test", "주간사장", "EMPLOYER")
        val (wid, code) = createWorkplace(bossToken, "주간 매장")
        val (empToken, empId) = signupReturning("weeklyemp@ptmanager.test", "주간알바", "EMPLOYEE")
        joinAndApprove(empToken, code, bossToken)

        mockMvc.perform(
            patch("/api/workplaces/$wid/members/$empId/wage")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $bossToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"hourlyWage":10000}"""),
        ).andExpect(status().isOk)

        // 1주(3일, 5시간→50000) + 3주(18일, 4시간→40000). 2·4주는 0이어야 한다.
        createShiftOn(bossToken, wid, empId, "2026-03-03", "09:00:00", "14:00:00")
        createShiftOn(bossToken, wid, empId, "2026-03-18", "09:00:00", "13:00:00")

        mockMvc.perform(
            get("/api/payroll/weekly?workplaceId=$wid&yearMonth=2026-03")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $bossToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.weeks.length()", `is`(4)))
            .andExpect(jsonPath("$.weeks[0].amount", `is`(50000)))
            .andExpect(jsonPath("$.weeks[1].amount", `is`(0)))
            .andExpect(jsonPath("$.weeks[2].amount", `is`(40000)))
            .andExpect(jsonPath("$.weeks[3].amount", `is`(0)))
    }

    private fun createShiftOn(
        employerToken: String,
        workplaceId: Long,
        employeeId: Long,
        workDate: String,
        start: String,
        end: String,
    ) {
        mockMvc.perform(
            post("/api/shifts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $employerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"workplaceId":$workplaceId,"employeeId":$employeeId,"workDate":"$workDate","startTime":"$start","endTime":"$end"}""",
                ),
        ).andExpect(status().isCreated)
    }

    @Test
    fun noticeResponseIncludesAuthorNameAndAttachments() {
        // 새 사장 가입 → 매장 생성(자동 소속) → 첨부 포함 공지 작성 → 응답 검증
        val signup = mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"noticeboss@ptmanager.test","password":"password1","name":"공지사장","role":"EMPLOYER"}"""),
        )
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
        val token: String = JsonPath.read(signup, "$.accessToken")

        val workplace = mockMvc.perform(
            post("/api/workplaces")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"공지 테스트 매장"}"""),
        )
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
        val workplaceId: Int = JsonPath.read(workplace, "$.id")

        mockMvc.perform(
            post("/api/notices")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "workplaceId": $workplaceId,
                      "title": "안내",
                      "body": "본문",
                      "attachmentUrls": ["https://example.com/a.jpg"]
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.authorName", `is`("공지사장")))
            .andExpect(jsonPath("$.attachments[0].fileUrl", `is`("https://example.com/a.jpg")))
    }

    @Test
    fun notificationInboxUsesIsReadAndIsOwnerScoped() {
        // 사장 E 가입 + 매장 생성
        val eSignup = mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"notif-boss@ptmanager.test","password":"password1","name":"알림사장","role":"EMPLOYER"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val eToken: String = JsonPath.read(eSignup, "$.accessToken")

        val workplace = mockMvc.perform(
            post("/api/workplaces")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $eToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"알림 테스트 매장"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val inviteCode: String = JsonPath.read(workplace, "$.inviteCode")

        // 직원 M 가입 + 가입 신청
        val mSignup = mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"notif-emp@ptmanager.test","password":"password1","name":"알림직원","role":"EMPLOYEE"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val mToken: String = JsonPath.read(mSignup, "$.accessToken")

        val joinRequest = mockMvc.perform(
            post("/api/join-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $mToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"inviteCode":"$inviteCode"}"""),
        ).andExpect(status().isCreated).andReturn().response.contentAsString
        val joinRequestId: Int = JsonPath.read(joinRequest, "$.id")

        // E 가 승인 → M 에게 JOIN_REQUEST 알림 생성
        mockMvc.perform(
            patch("/api/join-requests/$joinRequestId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $eToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"decision":"APPROVE"}"""),
        ).andExpect(status().isOk)

        // M 인박스: 응답 필드가 isRead 인지 (read 아님) 확인
        val inbox = mockMvc.perform(
            get("/api/notifications").header(HttpHeaders.AUTHORIZATION, "Bearer $mToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].isRead", `is`(false)))
            .andExpect(jsonPath("$.content[0].read").doesNotExist())
            .andReturn().response.contentAsString
        val notificationId: Int = JsonPath.read(inbox, "$.content[0].id")

        // 소유권: E 가 M 의 알림을 읽음 처리 시도 → 404
        mockMvc.perform(
            patch("/api/notifications/$notificationId/read")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $eToken"),
        ).andExpect(status().isNotFound)

        // 본인(M)은 읽음 처리 가능 → 204, 이후 안 읽은 수 0
        mockMvc.perform(
            patch("/api/notifications/$notificationId/read")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $mToken"),
        ).andExpect(status().isNoContent)
        mockMvc.perform(
            get("/api/notifications/unread-count").header(HttpHeaders.AUTHORIZATION, "Bearer $mToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count", `is`(0)))
    }

    @Test
    fun checkInWithValidQrTokenSucceeds() {
        val token = loginAs("employee@ptmanager.test")
        val qrToken = qrCodeService.issue(1) // 매장 1의 QR
        mockMvc.perform(
            post("/api/shifts/1/check-in")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"qrToken":"$qrToken"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.checkedInAt").exists())
            .andExpect(jsonPath("$.employeeName", `is`("Kim Employee")))
    }

    @Test
    fun joinRequestGuardsAndUserName() {
        val (eToken, _) = signupReturning("join-boss@ptmanager.test", "사장", "EMPLOYER")
        val (wId, invite) = createWorkplace(eToken, "가입 테스트 매장")
        val (mToken, _) = signupReturning("join-emp@ptmanager.test", "조이너", "EMPLOYEE")

        // 가입 신청 — 응답에 userName 포함
        val jr = mockMvc.perform(
            post("/api/join-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $mToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"inviteCode":"$invite"}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.userName", `is`("조이너")))
            .andReturn().response.contentAsString
        val joinId: Int = JsonPath.read(jr, "$.id")

        // B: 중복 신청 → 409
        mockMvc.perform(
            post("/api/join-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $mToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"inviteCode":"$invite"}"""),
        ).andExpect(status().isConflict)

        // A: 목록에도 userName
        mockMvc.perform(
            get("/api/join-requests?workplaceId=$wId").header(HttpHeaders.AUTHORIZATION, "Bearer $eToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].userName", `is`("조이너")))

        // 승인 → 200
        mockMvc.perform(
            patch("/api/join-requests/$joinId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $eToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"decision":"APPROVE"}"""),
        ).andExpect(status().isOk)

        // C: 이미 처리된 신청 재처리 → 409
        mockMvc.perform(
            patch("/api/join-requests/$joinId")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $eToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"decision":"REJECT"}"""),
        ).andExpect(status().isConflict)
    }

    @Test
    fun swapGuardsAndDetailShape() {
        val (eToken, _) = signupReturning("swap-boss@ptmanager.test", "사장", "EMPLOYER")
        val (wId, invite) = createWorkplace(eToken, "대타 테스트 매장")
        val (aToken, aId) = signupReturning("swap-a@ptmanager.test", "에이", "EMPLOYEE")
        val (bToken, bId) = signupReturning("swap-b@ptmanager.test", "비", "EMPLOYEE")
        val (cToken, _) = signupReturning("swap-c@ptmanager.test", "씨", "EMPLOYEE")
        joinAndApprove(aToken, invite, eToken)
        joinAndApprove(bToken, invite, eToken)
        joinAndApprove(cToken, invite, eToken)

        // 같은 날 겹치는 근무 2건 (A: 09-14, B: 10-15)
        val shiftA = createShift(eToken, wId, aId, "09:00:00", "14:00:00")
        val shiftB = createShift(eToken, wId, bId, "10:00:00", "15:00:00")

        val swapA = createSwap(aToken, shiftA)
        // ① 같은 근무에 PENDING 중복 → 409
        mockMvc.perform(
            post("/api/swap-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $aToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"shiftId":$shiftA,"reason":"중복"}"""),
        ).andExpect(status().isConflict)

        val swapB = createSwap(bToken, shiftB)

        // C가 swapA에 지원(201) 후, 겹치는 swapB에 지원 → ② 교차 더블부킹 400
        mockMvc.perform(
            post("/api/swap-requests/$swapA/applications").header(HttpHeaders.AUTHORIZATION, "Bearer $cToken"),
        ).andExpect(status().isCreated)
        mockMvc.perform(
            post("/api/swap-requests/$swapB/applications").header(HttpHeaders.AUTHORIZATION, "Bearer $cToken"),
        ).andExpect(status().isBadRequest)

        // ③ 상세 응답 평면화 + ④ applicantName
        mockMvc.perform(
            get("/api/swap-requests/$swapA").header(HttpHeaders.AUTHORIZATION, "Bearer $eToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.shiftId", `is`(shiftA.toInt())))
            .andExpect(jsonPath("$.status", `is`("PENDING")))
            .andExpect(jsonPath("$.request").doesNotExist())
            .andExpect(jsonPath("$.applications[0].applicantName", `is`("씨")))
    }

    @Test
    fun shiftOverlapRejectedAndDraftsHiddenUntilPublished() {
        val (eToken, _) = signupReturning("pub-boss@ptmanager.test", "사장", "EMPLOYER")
        val (wId, invite) = createWorkplace(eToken, "발행 테스트 매장")
        val (empToken, empId) = signupReturning("pub-emp@ptmanager.test", "알바", "EMPLOYEE")
        joinAndApprove(empToken, invite, eToken)

        // 09-14 편성(초안), 겹치는 13-18 → 409, 맞닿는 14-18 → OK(경계는 겹침 아님)
        createShift(eToken, wId, empId, "09:00:00", "14:00:00")
        mockMvc.perform(
            post("/api/shifts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $eToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"workplaceId":$wId,"employeeId":$empId,"workDate":"2026-07-01","startTime":"13:00:00","endTime":"18:00:00"}"""),
        ).andExpect(status().isConflict)
        createShift(eToken, wId, empId, "14:00:00", "18:00:00")

        // 발행 전: 직원에게는 초안이 보이지 않는다.
        mockMvc.perform(
            get("/api/shifts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $empToken")
                .param("employeeId", "me").param("from", "2026-07-01").param("to", "2026-07-01"),
        ).andExpect(status().isOk).andExpect(jsonPath("$.length()", `is`(0)))

        // 발행 → 2건, 직원에게 공개
        mockMvc.perform(
            post("/api/shifts/publish")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $eToken")
                .param("workplaceId", wId.toString()).param("from", "2026-07-01").param("to", "2026-07-01"),
        ).andExpect(status().isOk).andExpect(jsonPath("$.published", `is`(2)))
        mockMvc.perform(
            get("/api/shifts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $empToken")
                .param("employeeId", "me").param("from", "2026-07-01").param("to", "2026-07-01"),
        ).andExpect(status().isOk).andExpect(jsonPath("$.length()", `is`(2)))
    }

    @Test
    fun cannotDeleteShiftWithPendingSwap() {
        // 직원이 shift 2 에 대타 요청(PENDING) 생성
        val empToken = loginAs("employee@ptmanager.test")
        mockMvc.perform(
            post("/api/swap-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $empToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"shiftId":2,"reason":"삭제 테스트"}"""),
        ).andExpect(status().isCreated)

        // 사장이 삭제 시도 → 409
        val bossToken = loginAs("employer@ptmanager.test")
        mockMvc.perform(
            delete("/api/shifts/2").header(HttpHeaders.AUTHORIZATION, "Bearer $bossToken"),
        ).andExpect(status().isConflict)
    }

    @Test
    fun checkInWithForgedQrTokenIsRejected() {
        val token = loginAs("employee@ptmanager.test")
        mockMvc.perform(
            post("/api/shifts/2/check-in")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"qrToken":"wp1:1719740400:deadbeef"}"""),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun createSwapRequestReturnsPendingStatus() {
        val token = loginAs("employee@ptmanager.test")
        mockMvc.perform(
            post("/api/swap-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "shiftId": 1,
                      "reason": "Personal schedule"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status", `is`("PENDING")))
    }

    @Test
    fun qrTokenRejectedPastMaxAge() {
        val now = Instant.now().epochSecond
        // 만료 직전(59초)은 통과, 만료 후(61초)는 거부. (qr.max-age-seconds=60)
        qrCodeService.verify(QR_TEST_WORKPLACE, qrCodeService.issue(QR_TEST_WORKPLACE, now - 59))
        val expired = assertThrows<IllegalArgumentException> {
            qrCodeService.verify(QR_TEST_WORKPLACE, qrCodeService.issue(QR_TEST_WORKPLACE, now - 61))
        }
        assertTrue(expired.message!!.contains("만료"))
    }

    @Test
    fun secondApproveIsRejectedAndFirstSubstituteKept() {
        val (eToken, _) = signupReturning("dup-boss@ptmanager.test", "사장", "EMPLOYER")
        val (wId, invite) = createWorkplace(eToken, "동시 승인 매장")
        val (aToken, aId) = signupReturning("dup-a@ptmanager.test", "에이", "EMPLOYEE")
        val (bToken, bId) = signupReturning("dup-b@ptmanager.test", "비", "EMPLOYEE")
        val (cToken, cId) = signupReturning("dup-c@ptmanager.test", "씨", "EMPLOYEE")
        joinAndApprove(aToken, invite, eToken)
        joinAndApprove(bToken, invite, eToken)
        joinAndApprove(cToken, invite, eToken)

        val shift = createShift(eToken, wId, aId, "09:00:00", "14:00:00")
        val swap = createSwap(aToken, shift)
        listOf(bToken, cToken).forEach {
            mockMvc.perform(
                post("/api/swap-requests/$swap/applications").header(HttpHeaders.AUTHORIZATION, "Bearer $it"),
            ).andExpect(status().isCreated)
        }

        approve(eToken, swap, bId).andExpect(status().isOk)
        // 두 번째 승인은 markApproved 영향 행 0 → 409, 상태는 그대로 유지된다.
        approve(eToken, swap, cId).andExpect(status().isConflict)

        mockMvc.perform(get("/api/swap-requests/$swap").header(HttpHeaders.AUTHORIZATION, "Bearer $eToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", `is`("APPROVED")))
            .andExpect(jsonPath("$.substituteId", `is`(bId.toInt())))
            .andExpect(jsonPath("$.requesterId", `is`(aId.toInt())))
            .andExpect(jsonPath("$.shift.employeeId", `is`(bId.toInt())))
            .andExpect(jsonPath("$.applications[?(@.applicantId == $bId)].status", contains("APPROVED")))
            .andExpect(jsonPath("$.applications[?(@.applicantId == $cId)].status", contains("REJECTED")))
    }

    @Test
    fun myPayrollEndpointPreservesSummaryAndRejectsInvalidMonth() {
        val token = loginAs("employee@ptmanager.test")
        mockMvc.perform(get("/api/payroll/me?yearMonth=2020-01")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.yearMonth", `is`("2020-01")))
            .andExpect(jsonPath("$.workedMinutes", `is`(0)))
            .andExpect(jsonPath("$.amount", `is`(0)))
            .andExpect(jsonPath("$.weeks.length()", `is`(4)))
        mockMvc.perform(get("/api/payroll/me?yearMonth=invalid")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code", `is`("BAD_REQUEST")))
    }

    @Test
    fun attachmentUploadPreservesResponseAndEmployerOnlyAccess() {
        val file = MockMultipartFile("file", "note.txt", "text/plain", "handover".toByteArray())
        val employer = loginAs("employer@ptmanager.test")
        mockMvc.perform(multipart("/api/notices/attachments").file(file)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $employer"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.fileUrl").isString)
            .andExpect(jsonPath("$.noticeId", `is`(0)))
            .andExpect(jsonPath("$.createdAt").exists())
        val employee = loginAs("employee@ptmanager.test")
        mockMvc.perform(multipart("/api/notices/attachments").file(file)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $employee"))
            .andExpect(status().isForbidden)
    }

    private companion object {
        // 다른 테스트의 발급 기록과 섞이지 않도록 이 테스트 전용 매장 id를 쓴다.
        const val QR_TEST_WORKPLACE = 9001L
    }
}
