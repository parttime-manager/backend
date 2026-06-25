package com.ptmanager.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "workplace")
public class Workplace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String address;

    @Column(name = "invite_code", nullable = false, unique = true)
    private String inviteCode;

    protected Workplace() {
    }

    public Workplace(Long id, String name, String address, String inviteCode) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.inviteCode = inviteCode;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getInviteCode() {
        return inviteCode;
    }
}
