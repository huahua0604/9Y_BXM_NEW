package com.hospital.reim.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String employeeId;
    @NotBlank
    private String password;
}