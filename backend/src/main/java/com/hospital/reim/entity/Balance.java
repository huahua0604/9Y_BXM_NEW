package com.hospital.reim.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "balance")
@Getter @Setter
public class Balance {
    @Id
    private Long id = 1L;           // 全局单行，固定主键=1

    @Version
    private Long version;           // 乐观锁

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal available = BigDecimal.ZERO;  // 可用余额

    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    public void touch(){ updatedAt = LocalDateTime.now(); }
}
