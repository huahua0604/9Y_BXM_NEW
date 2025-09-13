package com.hospital.reim.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RequestCreateDto {
    @NotBlank
    private String title;
    @NotNull @DecimalMin(value = "0.01")
    private BigDecimal amount;
    @NotBlank
    private String category;
    @NotNull
    private LocalDate occurDate;
    @Size(max = 512)
    private String remark;
}
