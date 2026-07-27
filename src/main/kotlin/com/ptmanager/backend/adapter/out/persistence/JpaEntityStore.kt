package com.ptmanager.backend.adapter.out.persistence

import com.ptmanager.backend.application.port.out.EntityStore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface JpaEntityStore<T : Any, ID : Any> : JpaRepository<T, ID>, EntityStore<T, ID> {
    override fun findById(id: ID): java.util.Optional<T>
    override fun existsById(id: ID): Boolean
    override fun deleteById(id: ID)
}
