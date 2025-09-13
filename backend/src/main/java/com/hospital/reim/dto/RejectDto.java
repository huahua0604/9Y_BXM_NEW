package com.hospital.reim.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectDto {
    @NotBlank(message = "驳回原因不能为空")
    private String note;
}