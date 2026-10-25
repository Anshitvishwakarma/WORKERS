package com.fixmate.service.impl;

import com.fixmate.dto.AuthRequest;
import com.fixmate.dto.AuthResponse;
import com.fixmate.dto.RegisterRequest;
import com.fixmate.dto.UserSummaryDto;
import com.fixmate.entity.User;
import com.fixmate.enums.Role;
import com.fixmate.exception.BadRequestException;
import com.fixmate.exception.ConflictException;
import com.fixmate.exception.UnauthorizedException;
import com.fixmate.mapper.EntityMapper;
import com.fixmate.repository.UserRepository;
import com.fixmate.security.JwtUtil;
import com.fixmate.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EntityMapper entityMapper;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           EntityMapper entityMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 1. Never accept ADMIN as a public registration role
        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Administrator accounts cannot be created via public registration.");
        }

        // 2. Validate email uniqueness
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with email '" + email + "' already exists.");
        }

        // 3. Create user entity
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());
        user.setAddress(request.getAddress().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);

        User savedUser = userRepository.save(user);
        log.info("Registered new user with email: {} and role: {}", savedUser.getEmail(), savedUser.getRole());

        // 4. Generate JWT
        String token = jwtUtil.generateToken(savedUser);
        UserSummaryDto userSummary = entityMapper.toUserSummaryDto(savedUser);

        return new AuthResponse(token, "Bearer", userSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account has been deactivated. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        log.info("User logged in successfully: {}", email);
        String token = jwtUtil.generateToken(user);
        UserSummaryDto userSummary = entityMapper.toUserSummaryDto(user);

        return new AuthResponse(token, "Bearer", userSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found."));
        return entityMapper.toUserSummaryDto(user);
    }
}
