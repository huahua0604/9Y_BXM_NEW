package com.hospital.reim.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "fund_tx")
@Getter @Setter
public class FundTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TxType type;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @ManyToOne(optional = false)
    private User operator;

    @ManyToOne
    private ReimburseRequest relatedRequest;

    @Column(length = 256)
    private String note;

    public enum TxType { ADD, DEDUCT }
}

