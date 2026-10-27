package com.cagdasbilgin.twitterclone.service;

import com.cagdasbilgin.twitterclone.dto.AuthResponse;
import com.cagdasbilgin.twitterclone.dto.LoginRequest;
import com.cagdasbilgin.twitterclone.dto.RegisterRequest;
import com.cagdasbilgin.twitterclone.dto.UserResponse;
import com.cagdasbilgin.twitterclone.entity.User;

public interface UserService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    User getAuthenticatedUser();
    User getUserById(Long id);
    User getUserByUsername(String username);
}
