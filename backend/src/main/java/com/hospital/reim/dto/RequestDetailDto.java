package com.hospital.reim.dto;

import com.hospital.reim.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class RequestDetailDto {
    private Long id;
    private String title;
    private BigDecimal amount;
    private String category;
    private LocalDate occurDate;
    private String remark;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private List<AttachmentDto> attachments;
}
