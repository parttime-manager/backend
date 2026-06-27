package com.ptmanager.backend.joinrequest.dto

import com.ptmanager.backend.domain.JoinRequestStatus

data class UpdateJoinRequestStatus(
    val status: JoinRequestStatus,
)
