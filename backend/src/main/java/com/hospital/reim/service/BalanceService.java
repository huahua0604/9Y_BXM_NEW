package com.hospital.reim.service;

import com.hospital.reim.entity.*;
import com.hospital.reim.exception.BadRequestException;
import com.hospital.reim.repository.BalanceRepository;
import com.hospital.reim.repository.FundTransactionRepository;
import com.hospital.reim.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepo;
    private final FundTransactionRepository txRepo;
    private final UserRepository userRepo;

    @Transactional
    public void addFunds(String operatorEmpId, BigDecimal amount, String note) {
        addFunds(operatorEmpId, amount, note, null);
    }
    
    @Transactional
    public Balance getOrInit() {
        return balanceRepo.findById(1L).orElseGet(() -> {
            Balance b = new Balance();
            b.setId(1L);
            b.setAvailable(BigDecimal.ZERO);
            return balanceRepo.save(b);
        });
    }

    /** 管理员加款 */
    @Transactional
    public void addFunds(String operatorEmpId, BigDecimal amount, String note, LocalDateTime occurredAt) {
        if (amount == null || amount.signum() <= 0) throw new BadRequestException("金额必须为正数");
        User op = userRepo.findByEmployeeId(operatorEmpId).orElseThrow();
        if (!op.getRoles().contains(Role.ADMIN)) throw new BadRequestException("仅管理员可加款");

        Balance b = getOrInit(); // 带 @Version 乐观锁
        b.setAvailable(b.getAvailable().add(amount));
        balanceRepo.save(b);

        FundTransaction tx = new FundTransaction();
        tx.setType(FundTransaction.TxType.ADD);
        tx.setAmount(amount);
        tx.setOperator(op);
        tx.setNote(note);
        tx.setOccurredAt(occurredAt != null ? occurredAt : LocalDateTime.now());
        txRepo.save(tx);
    }

    /** 审批通过扣款（与审批同一事务调用） */
    @Transactional
    public void deductForApproval(User reviewer, ReimburseRequest req) {
        BigDecimal amt = req.getAmount();
        if (amt == null || amt.signum() <= 0) throw new BadRequestException("报销金额非法");

        Balance b = getOrInit(); // 乐观锁，避免并发超扣
        if (b.getAvailable().compareTo(amt) < 0) {
            throw new BadRequestException("余额不足，当前可用：" + b.getAvailable());
        }
        b.setAvailable(b.getAvailable().subtract(amt));
        balanceRepo.save(b);

        FundTransaction tx = new FundTransaction();
        tx.setType(FundTransaction.TxType.DEDUCT);
        tx.setAmount(amt);
        tx.setOperator(reviewer);
        tx.setRelatedRequest(req);
        tx.setNote("审批通过自动扣减");
        tx.setOccurredAt(LocalDateTime.now());
        txRepo.save(tx);
    }
}

