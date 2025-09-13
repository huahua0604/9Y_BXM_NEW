package com.hospital.reim.repository;

import com.hospital.reim.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> { 
    List<Attachment> findByRequestId(Long requestId);
}