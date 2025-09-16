package com.hospital.reim.controller;

import com.hospital.reim.dto.AddFundsRequest;
import com.hospital.reim.entity.Balance;
import com.hospital.reim.entity.FundTransaction;
import com.hospital.reim.repository.FundTransactionRepository;
import com.hospital.reim.service.BalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/balance")
@RequiredArgsConstructor
public class BalanceAdminController {

    private final BalanceService balanceService;
    private final FundTransactionRepository txRepo;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Balance get() {
        return balanceService.getOrInit();
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> add(@Valid @RequestBody AddFundsRequest req, Authentication auth) {
        balanceService.addFunds(auth.getName(), req.getAmount(), req.getNote(), req.getOccurredAt());
        return ResponseEntity.ok(Map.of("message", "加款成功"));
    }

    @GetMapping("/tx")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FundTransaction> latestTx() {
        return txRepo.findTop50ByOrderByOccurredAtDesc();
    }
}