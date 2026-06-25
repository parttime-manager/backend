package com.ptmanager.backend.shift;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ptmanager.backend.domain.Shift;
import com.ptmanager.backend.repository.ShiftRepository;
import com.ptmanager.backend.repository.UserRepository;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    public ShiftService(ShiftRepository shiftRepository, UserRepository userRepository) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    public List<Shift> findShiftsByUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found.");
        }
        return shiftRepository.findByEmployeeIdOrderByWorkDateAscStartTimeAsc(userId);
    }

    @Transactional
    public Shift checkIn(long shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
            .orElseThrow(() -> new NoSuchElementException("Shift not found."));
        if (shift.isCheckedIn()) {
            throw new IllegalArgumentException("Shift is already checked in.");
        }
        shift.setCheckedIn(true);
        return shiftRepository.save(shift);
    }
}
