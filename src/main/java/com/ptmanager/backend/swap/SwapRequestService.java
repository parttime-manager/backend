package com.ptmanager.backend.swap;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptmanager.backend.domain.NotificationType;
import com.ptmanager.backend.domain.SwapRequest;
import com.ptmanager.backend.domain.SwapRequestStatus;
import com.ptmanager.backend.domain.User;
import com.ptmanager.backend.notification.NotificationService;
import com.ptmanager.backend.repository.SwapRequestRepository;
import com.ptmanager.backend.repository.UserRepository;

@Service
public class SwapRequestService {

    private final SwapRequestRepository swapRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public SwapRequestService(SwapRequestRepository swapRequestRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.swapRequestRepository = swapRequestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<SwapRequest> findSwapRequests() {
        return swapRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public SwapRequest createSwapRequest(long requesterId, Long substituteId, LocalDate workDate, String reason) {
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new NoSuchElementException("Requester not found."));
        if (substituteId != null && !userRepository.existsById(substituteId)) {
            throw new NoSuchElementException("Substitute user not found.");
        }
        SwapRequest request = new SwapRequest(
            null,
            requester.getWorkplaceId(),
            requesterId,
            substituteId,
            workDate,
            reason,
            SwapRequestStatus.PENDING,
            Instant.now()
        );
        return swapRequestRepository.save(request);
    }

    @Transactional
    public SwapRequest updateStatus(long id, SwapRequestStatus status) {
        SwapRequest request = swapRequestRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Swap request not found."));
        request.setStatus(status);
        SwapRequest saved = swapRequestRepository.save(request);
        notificationService.notify(
            saved.getRequesterId(),
            NotificationType.SWAP_RESULT,
            "대타 요청이 " + status.name() + " 처리되었습니다."
        );
        return saved;
    }
}
