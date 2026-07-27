package com.ptmanager.backend.application.port.out

import java.util.Optional

/** Persistence operations required by application use cases; implemented by the JPA adapter. */
interface EntityStore<T : Any, ID : Any> {
    fun findById(id: ID): Optional<T>
    fun findAll(): List<T>
    fun findAllById(ids: Iterable<ID>): List<T>
    fun existsById(id: ID): Boolean
    fun count(): Long
    fun <S : T> save(entity: S): S
    fun <S : T> saveAll(entities: Iterable<S>): List<S>
    fun delete(entity: T)
    fun deleteById(id: ID)
    fun deleteAll(entities: Iterable<T>)
    fun deleteAll()
}
