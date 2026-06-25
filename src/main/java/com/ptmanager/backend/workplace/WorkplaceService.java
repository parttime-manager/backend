package com.ptmanager.backend.workplace;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptmanager.backend.domain.Workplace;
import com.ptmanager.backend.repository.WorkplaceRepository;

@Service
public class WorkplaceService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final WorkplaceRepository workplaceRepository;

    public WorkplaceService(WorkplaceRepository workplaceRepository) {
        this.workplaceRepository = workplaceRepository;
    }

    public List<Workplace> findWorkplaces() {
        return workplaceRepository.findAll();
    }

    @Transactional
    public Workplace createWorkplace(String name, String address) {
        Workplace workplace = new Workplace(null, name, address, generateUniqueInviteCode());
        return workplaceRepository.save(workplace);
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            StringBuilder builder = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                builder.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            code = builder.toString();
        } while (workplaceRepository.existsByInviteCode(code));
        return code;
    }
}
