package com.hospital.reim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class ReturnDto { @NotBlank @Size(max=512) private String note; }
