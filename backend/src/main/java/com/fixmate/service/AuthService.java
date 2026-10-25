package com.fixmate.service;

import com.fixmate.dto.AuthRequest;
import com.fixmate.dto.AuthResponse;
import com.fixmate.dto.RegisterRequest;
import com.fixmate.dto.UserSummaryDto;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    UserSummaryDto getCurrentUser(String email);
}
