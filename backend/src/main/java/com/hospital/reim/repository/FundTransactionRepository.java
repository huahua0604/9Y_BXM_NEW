package com.hospital.reim.repository;

import com.hospital.reim.entity.FundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {
    List<FundTransaction> findTop50ByOrderByOccurredAtDesc();
}