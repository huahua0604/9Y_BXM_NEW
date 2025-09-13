package com.hospital.reim.repository;

import com.hospital.reim.entity.RequestAction;
import com.hospital.reim.entity.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {

    // 审核员（actor.employeeId）在时间范围内，且动作属于指定集合，按时间倒序
    List<RequestHistory> findByActor_EmployeeIdAndActionInAndOccurredAtBetweenOrderByOccurredAtDesc(
            String employeeId, List<RequestAction> actions, LocalDateTime start, LocalDateTime end
    );
    List<RequestHistory> findByRequest_IdOrderByOccurredAtAsc(Long requestId);
}
