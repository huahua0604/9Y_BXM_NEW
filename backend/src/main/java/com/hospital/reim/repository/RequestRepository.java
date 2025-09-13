package com.hospital.reim.repository;

import com.hospital.reim.entity.ReimburseRequest;
import com.hospital.reim.entity.RequestStatus;
import com.hospital.reim.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<ReimburseRequest, Long> {
    List<ReimburseRequest> findByApplicantOrderByCreatedAtDesc(User applicant);
    List<ReimburseRequest> findByStatusOrderBySubmittedAtAsc(RequestStatus status);
}
