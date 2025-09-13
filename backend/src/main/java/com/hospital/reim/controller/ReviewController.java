package com.hospital.reim.controller;

import com.hospital.reim.dto.ApproveDto;
import com.hospital.reim.dto.RejectDto;
import com.hospital.reim.dto.RequestListItemDto;
import com.hospital.reim.dto.ReturnDto;
import com.hospital.reim.entity.RequestHistory;
import com.hospital.reim.service.RequestService;
import com.hospital.reim.dto.ApprovalHistoryDto;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
public class ReviewController {
    private final RequestService requestService;

    @GetMapping("/queue")
    public List<RequestListItemDto> queue() {
        return requestService.reviewQueue();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                     @Valid @RequestBody ApproveDto dto,
                                     Authentication auth) {
        requestService.approve(auth.getName(), id, dto.getNote());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                    @Valid @RequestBody RejectDto dto,
                                    Authentication auth) {
        requestService.reject(auth.getName(), id, dto.getNote());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> sendBack(@PathVariable Long id,
                                      @Valid @RequestBody ReturnDto dto,
                                      Authentication auth) {
        requestService.sendBack(auth.getName(), id, dto.getNote());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-approvals")
    @PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
    public List<ApprovalHistoryDto> myApprovals(
        Authentication auth,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return requestService.myApprovalHistory(auth.getName(), startDate, endDate);
    }
}
