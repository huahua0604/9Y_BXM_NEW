package com.hospital.reim.controller;

import com.hospital.reim.entity.Attachment;
import com.hospital.reim.entity.ReimburseRequest;
import com.hospital.reim.entity.User;
import com.hospital.reim.exception.NotFoundException;
import com.hospital.reim.repository.AttachmentRepository;
import com.hospital.reim.repository.RequestRepository;
import com.hospital.reim.repository.UserRepository;
import com.hospital.reim.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentRepository attachmentRepo;
    private final RequestRepository requestRepo;
    private final UserRepository userRepo;
    private final StorageService storageService;

    @GetMapping("/{id}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long id, Authentication auth) {
        Attachment att = attachmentRepo.findById(id).orElseThrow(() -> new NotFoundException("附件不存在"));
        ReimburseRequest req = requestRepo.findById(att.getRequest().getId())
                .orElseThrow(() -> new NotFoundException("单据不存在"));
        User me = userRepo.findByEmployeeId(auth.getName()).orElseThrow();

        boolean owner = req.getApplicant().getId().equals(me.getId());
        boolean reviewer = me.getRoles().stream().anyMatch(r -> r.name().equals("REVIEWER") || r.name().equals("ADMIN"));
        if (!owner && !reviewer) return ResponseEntity.status(403).build();

        Path path = storageService.resolvePath(att);
        FileSystemResource res = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + att.getOriginalFilename() + "\"")
                .body(res);
    }
}

