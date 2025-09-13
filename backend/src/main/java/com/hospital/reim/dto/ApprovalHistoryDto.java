package com.hospital.reim.dto;

import com.hospital.reim.entity.RequestAction;
import com.hospital.reim.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApprovalHistoryDto {
    private Long historyId;
    private Long requestId;
    private String title;
    private BigDecimal amount;
    private RequestStatus requestStatus;   // 审批后单据状态
    private RequestAction action;          // 审批动作：APPROVE/REJECT/RETURN
    private String note;                   // 审批备注
    private LocalDateTime occurredAt;      // 审批时间
}