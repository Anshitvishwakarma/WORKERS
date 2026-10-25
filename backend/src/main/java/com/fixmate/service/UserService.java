package com.fixmate.service;

import com.fixmate.dto.PasswordChangeRequest;
import com.fixmate.dto.UserProfileUpdateRequest;
import com.fixmate.dto.UserSummaryDto;

public interface UserService {
    UserSummaryDto updateProfile(String userEmail, UserProfileUpdateRequest request);
    void changePassword(String userEmail, PasswordChangeRequest request);
}
