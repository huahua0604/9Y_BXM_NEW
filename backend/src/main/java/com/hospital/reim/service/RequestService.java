package com.hospital.reim.service;

import com.hospital.reim.dto.ApprovalHistoryDto;
import com.hospital.reim.dto.RequestCreateDto;
import com.hospital.reim.dto.RequestDetailDto;
import com.hospital.reim.dto.RequestListItemDto;
import com.hospital.reim.entity.*;
import com.hospital.reim.exception.BadRequestException;
import com.hospital.reim.exception.NotFoundException;
import com.hospital.reim.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final UserRepository userRepo;
    private final RequestRepository requestRepo;
    private final AttachmentRepository attachmentRepo;
    private final RequestHistoryRepository historyRepo;
    private final StorageService storageService;
    private final UserService userService;

    public List<RequestListItemDto> listMy(String employeeId) {
        User me = userRepo.findByEmployeeId(employeeId).orElseThrow();
        return requestRepo.findByApplicantOrderByCreatedAtDesc(me).stream()
                .map(r -> new RequestListItemDto(r.getId(), r.getTitle(), r.getAmount(), r.getStatus(), r.getCreatedAt()))
                .toList();
    }
    
    public List<RequestListItemDto> reviewQueue() {
    return requestRepo.findByStatusOrderBySubmittedAtAsc(RequestStatus.SUBMITTED)
            .stream()
            .map(r -> new RequestListItemDto(
                    r.getId(),
                    r.getTitle(),
                    r.getAmount(),
                    r.getStatus(),
                    r.getCreatedAt()
            ))
            .toList();
    }


    @Transactional
    public Long create(String employeeId, RequestCreateDto dto) {
        User me = userRepo.findByEmployeeId(employeeId).orElseThrow();
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("金额必须大于 0");
        }
        ReimburseRequest r = new ReimburseRequest();
        r.setApplicant(me);
        r.setStatus(RequestStatus.DRAFT);
        r.setTitle(dto.getTitle());
        r.setAmount(dto.getAmount());
        r.setCategory(dto.getCategory());
        r.setOccurDate(dto.getOccurDate());
        r.setRemark(dto.getRemark());
        r.setCreatedAt(LocalDateTime.now());
        r = requestRepo.save(r);

        saveHistory(r, me, RequestAction.CREATE, "创建草稿");
        return r.getId();
    }

    public RequestDetailDto detail(String employeeId, Long id) {
        ReimburseRequest r = requestRepo.findById(id).orElseThrow(() -> new NotFoundException("单据不存在"));
        boolean owner = r.getApplicant().getEmployeeId().equals(employeeId);
        if (!owner && !userService.hasReviewer(employeeId)) throw new AccessDeniedException("无权限");

        var atts = attachmentRepo.findByRequestId(id).stream()
                .map(a -> new com.hospital.reim.dto.AttachmentDto(a.getId(), a.getOriginalFilename(), a.getSize()))
                .toList();

        return new RequestDetailDto(r.getId(), r.getTitle(), r.getAmount(), r.getCategory(),
                r.getOccurDate(), r.getRemark(), r.getStatus(),
                r.getCreatedAt(), r.getSubmittedAt(), r.getReviewedAt(), atts);
    }

    @Transactional
    public void submit(String employeeId, Long id) {
        ReimburseRequest r = requestRepo.findById(id).orElseThrow(() -> new NotFoundException("单据不存在"));
        User me = userRepo.findByEmployeeId(employeeId).orElseThrow();
        if (!r.getApplicant().getId().equals(me.getId())) throw new AccessDeniedException("无权限");
        if (!(r.getStatus() == RequestStatus.DRAFT || r.getStatus() == RequestStatus.RETURNED)) {
            throw new BadRequestException("当前状态不可提交");
        }
        r.setStatus(RequestStatus.SUBMITTED);
        r.setSubmittedAt(LocalDateTime.now());
        requestRepo.save(r);
        saveHistory(r, me, RequestAction.SUBMIT, "提交审核");
    }

    @Transactional
    public Long uploadAttachment(String employeeId, Long requestId, MultipartFile file) {
        ReimburseRequest r = requestRepo.findById(requestId).orElseThrow(() -> new NotFoundException("单据不存在"));
        boolean owner = r.getApplicant().getEmployeeId().equals(employeeId);
        if (!owner && !userService.hasReviewer(employeeId)) throw new AccessDeniedException("无权限");

        var stored = storageService.savePdf(file); // 校验 PDF & 保存
        Attachment a = new Attachment();
        a.setRequest(r);
        a.setOriginalFilename(stored.originalFilename());
        a.setStoredFilename(stored.storedFilename());
        a.setStoragePath(stored.storagePath());
        a.setContentType(stored.contentType());
        a.setSize(stored.size());
        a.setSha256(stored.sha256());
        a.setUploadedAt(LocalDateTime.now());
        a = attachmentRepo.save(a);

        saveHistory(r, r.getApplicant(), RequestAction.UPLOAD_ATTACHMENT, "上传附件：" + a.getOriginalFilename());
        return a.getId();
    }

    @Transactional
    public void approve(String reviewerEmpId, Long id, String note) {
        ReimburseRequest r = requestRepo.findById(id).orElseThrow(() -> new NotFoundException("单据不存在"));
        User reviewer = userRepo.findByEmployeeId(reviewerEmpId).orElseThrow();
        if (!userService.hasReviewer(reviewerEmpId)) throw new AccessDeniedException("无权限");
        if (r.getStatus() != RequestStatus.SUBMITTED) throw new BadRequestException("当前状态不可通过");
        r.setStatus(RequestStatus.APPROVED);
        r.setReviewedAt(LocalDateTime.now());
        requestRepo.save(r);
        saveHistory(r, reviewer, RequestAction.APPROVE, note);
    }

    @Transactional
    public void reject(String reviewerEmpId, Long id, String note) {
        ReimburseRequest r = requestRepo.findById(id).orElseThrow(() -> new NotFoundException("单据不存在"));
        User reviewer = userRepo.findByEmployeeId(reviewerEmpId).orElseThrow();
        if (!userService.hasReviewer(reviewerEmpId)) throw new AccessDeniedException("无权限");
        if (r.getStatus() != RequestStatus.SUBMITTED) throw new BadRequestException("当前状态不可驳回");
        r.setStatus(RequestStatus.REJECTED);
        r.setReviewedAt(LocalDateTime.now());
        requestRepo.save(r);
        saveHistory(r, reviewer, RequestAction.REJECT, note);
    }

    @Transactional
    public void sendBack(String reviewerEmpId, Long id, String note) {
        ReimburseRequest r = requestRepo.findById(id).orElseThrow(() -> new NotFoundException("单据不存在"));
        User reviewer = userRepo.findByEmployeeId(reviewerEmpId).orElseThrow();
        if (!userService.hasReviewer(reviewerEmpId)) throw new AccessDeniedException("无权限");
        if (r.getStatus() != RequestStatus.SUBMITTED) throw new BadRequestException("当前状态不可退回");
        r.setStatus(RequestStatus.RETURNED);
        requestRepo.save(r);
        saveHistory(r, reviewer, RequestAction.RETURN, note);
    }

    public List<ApprovalHistoryDto> myApprovalHistory(String reviewerEmpId, LocalDate startDate, LocalDate endDate) {
    LocalDate s = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
    LocalDate e = (endDate   != null) ? endDate   : LocalDate.now();

    var actions = List.of(RequestAction.APPROVE, RequestAction.REJECT, RequestAction.RETURN);

    var list = historyRepo.findByActor_EmployeeIdAndActionInAndOccurredAtBetweenOrderByOccurredAtDesc(
            reviewerEmpId,
            actions,
            LocalDateTime.of(s, LocalTime.MIN),
            LocalDateTime.of(e, LocalTime.MAX)
    );

    return list.stream().map(h -> {
        var r = h.getRequest();
        return new ApprovalHistoryDto(
                h.getId(),
                r.getId(),
                r.getTitle(),
                r.getAmount(),
                r.getStatus(),
                h.getAction(),
                h.getNote(),
                h.getOccurredAt()
        );
    }).toList();
    }

    public List<RequestHistory> history(Long requestId) {
        return historyRepo.findByRequest_IdOrderByOccurredAtAsc(requestId);
    }

    private void saveHistory(ReimburseRequest r, User actor, RequestAction action, String note) {
        RequestHistory h = new RequestHistory();
        h.setRequest(r);
        h.setActor(actor);
        h.setAction(action);
        h.setNote(note);
        historyRepo.save(h);
    }
}
