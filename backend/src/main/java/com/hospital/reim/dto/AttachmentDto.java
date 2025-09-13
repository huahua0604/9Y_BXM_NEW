package com.hospital.reim.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttachmentDto {
    private Long id;
    private String filename;
    private long size;
}
