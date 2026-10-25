package com.fixmate.service.impl;

import com.fixmate.dto.PasswordChangeRequest;
import com.fixmate.dto.UserProfileUpdateRequest;
import com.fixmate.dto.UserSummaryDto;
import com.fixmate.entity.User;
import com.fixmate.exception.BadRequestException;
import com.fixmate.exception.ResourceNotFoundException;
import com.fixmate.mapper.EntityMapper;
import com.fixmate.repository.UserRepository;
import com.fixmate.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityMapper entityMapper;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           EntityMapper entityMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional
    public UserSummaryDto updateProfile(String userEmail, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmailIgnoreCase(userEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        user.setAddress(request.getAddress().trim());

        User updatedUser = userRepository.save(user);
        return entityMapper.toUserSummaryDto(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(String userEmail, PasswordChangeRequest request) {
        User user = userRepository.findByEmailIgnoreCase(userEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
