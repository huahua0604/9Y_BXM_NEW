package com.hospital.reim.controller;

import com.hospital.reim.dto.RequestCreateDto;
import com.hospital.reim.dto.RequestDetailDto;
import com.hospital.reim.dto.RequestListItemDto;
import com.hospital.reim.entity.RequestHistory;
import com.hospital.reim.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @GetMapping
    public List<RequestListItemDto> myList(Authentication auth) {
        return requestService.listMy(auth.getName());
    }

    @PostMapping
    public ResponseEntity<Long> create(@Valid @RequestBody RequestCreateDto dto, Authentication auth) {
        return ResponseEntity.ok(requestService.create(auth.getName(), dto));
    }

    @GetMapping("/{id}")
    public RequestDetailDto detail(@PathVariable Long id, Authentication auth) {
        return requestService.detail(auth.getName(), id);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id, Authentication auth) {
        requestService.submit(auth.getName(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<Long> upload(@PathVariable Long id,
                                       @RequestParam("file") MultipartFile file,
                                       Authentication auth) {
        return ResponseEntity.ok(requestService.uploadAttachment(auth.getName(), id, file));
    }

    @GetMapping("/{id}/history")
    public List<RequestHistory> history(@PathVariable Long id) {
        return requestService.history(id);
    }
}

