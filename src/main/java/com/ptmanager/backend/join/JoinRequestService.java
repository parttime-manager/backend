package com.ptmanager.backend.join;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptmanager.backend.domain.JoinRequest;
import com.ptmanager.backend.domain.JoinRequestStatus;
import com.ptmanager.backend.domain.NotificationType;
import com.ptmanager.backend.domain.User;
import com.ptmanager.backend.domain.Workplace;
import com.ptmanager.backend.notification.NotificationService;
import com.ptmanager.backend.repository.JoinRequestRepository;
import com.ptmanager.backend.repository.UserRepository;
import com.ptmanager.backend.repository.WorkplaceRepository;

@Service
public class JoinRequestService {

    private final JoinRequestRepository joinRequestRepository;
    private final WorkplaceRepository workplaceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public JoinRequestService(JoinRequestRepository joinRequestRepository,
                              WorkplaceRepository workplaceRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.joinRequestRepository = joinRequestRepository;
        this.workplaceRepository = workplaceRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<JoinRequest> findByWorkplace(long workplaceId) {
        return joinRequestRepository.findByWorkplaceIdOrderByCreatedAtDesc(workplaceId);
    }

    @Transactional
    public JoinRequest create(String inviteCode, long userId) {
        Workplace workplace = workplaceRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new NoSuchElementException("Invalid invite code."));
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found.");
        }
        JoinRequest request = new JoinRequest(
            null,
            workplace.getId(),
            userId,
            JoinRequestStatus.PENDING,
            Instant.now()
        );
        return joinRequestRepository.save(request);
    }

    @Transactional
    public JoinRequest updateStatus(long id, JoinRequestStatus status) {
        JoinRequest request = joinRequestRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Join request not found."));
        request.setStatus(status);
        JoinRequest saved = joinRequestRepository.save(request);

        if (status == JoinRequestStatus.APPROVED) {
            User user = userRepository.findById(saved.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found."));
            user.setWorkplaceId(saved.getWorkplaceId());
            userRepository.save(user);
        }
        notificationService.notify(
            saved.getUserId(),
            NotificationType.JOIN_REQUEST,
            "매장 가입 신청이 " + status.name() + " 처리되었습니다."
        );
        return saved;
    }
}
