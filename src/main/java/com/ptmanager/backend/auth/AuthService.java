package com.ptmanager.backend.auth;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.ptmanager.backend.domain.User;
import com.ptmanager.backend.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String email, String password) {
        if (!"password".equals(password)) {
            throw new IllegalArgumentException("Invalid credentials.");
        }
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new NoSuchElementException("User not found."));
    }
}
