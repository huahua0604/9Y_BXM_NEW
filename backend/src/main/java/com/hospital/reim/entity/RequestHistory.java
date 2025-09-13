package com.hospital.reim.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity @Table(name = "request_history")
@Getter @Setter
public class RequestHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ReimburseRequest request;

    @ManyToOne(optional = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestAction action;

    @Column(length = 512)
    private String note;

    private LocalDateTime occurredAt;

    @PrePersist
    public void prePersist() { occurredAt = LocalDateTime.now(); }
}
