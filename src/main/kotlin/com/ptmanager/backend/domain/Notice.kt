package com.ptmanager.backend.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "notice")
class Notice(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "workplace_id", nullable = false)
    var workplaceId: Long = 0,

    @Column(name = "author_id", nullable = false)
    var authorId: Long = 0,

    @Column(nullable = false)
    var title: String = "",

    @Lob
    @Column(nullable = false)
    var body: String = "",

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
)
