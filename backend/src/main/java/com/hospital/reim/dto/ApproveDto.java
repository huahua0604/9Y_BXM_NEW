package com.hospital.reim.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class ApproveDto { 
    @Size(max=512) private String note; 
}
