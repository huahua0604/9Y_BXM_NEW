package com.hospital.reim.dto;

import com.hospital.reim.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RequestListItemDto {
    private Long id;
    private String title;
    private BigDecimal amount;
    private RequestStatus status;
    private LocalDateTime createdAt;
}
