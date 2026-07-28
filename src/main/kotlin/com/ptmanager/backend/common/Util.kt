package com.ptmanager.backend.common

import com.ptmanager.backend.application.port.out.UserRepository
import java.time.LocalTime
import java.util.Optional

/** 값이 없으면 404로 매핑되는 NoSuchElementException을 던진다. (findById(...).orNotFound("X not found.")) */
fun <T> Optional<T>.orNotFound(message: String): T =
    orElseThrow { NoSuchElementException(message) }

/** 표시용 사용자 이름. 없으면 null. */
fun UserRepository.nameOf(userId: Long): String? =
    findById(userId).orElse(null)?.name

/** 표시용 사용자 이름 일괄 조회. 목록 응답에서 N+1 대신 한 번에 가져온다. */
fun UserRepository.namesOf(userIds: Collection<Long>): Map<Long?, String?> =
    findAllById(userIds.distinct()).associate { it.id to it.name }

/** 근무 시간(분). 야간 교대(end ≤ start)는 익일로 보정해 항상 양수로 계산한다. */
fun workedMinutes(start: LocalTime, end: LocalTime): Long {
    val startMin = (start.toSecondOfDay() / 60).toLong()
    val endMin = (end.toSecondOfDay() / 60).toLong()
    return if (endMin <= startMin) endMin + 24 * 60 - startMin else endMin - startMin
}
