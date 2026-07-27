package com.ptmanager.backend.adapter.out.security

import com.ptmanager.backend.application.port.out.CurrentUser
import com.ptmanager.backend.common.BusinessError
import com.ptmanager.backend.common.BusinessException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityCurrentUser : CurrentUser {
    override fun userId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw BusinessException(BusinessError.AUTHENTICATION_FAILED, "인증이 필요합니다.")
        return authentication.principal as? Long
            ?: throw BusinessException(BusinessError.AUTHENTICATION_FAILED, "인증이 필요합니다.")
    }

}
