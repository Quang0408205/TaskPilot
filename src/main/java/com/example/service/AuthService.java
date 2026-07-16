package com.example.service;

import com.example.dto.RegisterRequest;
import com.example.dto.LoginRequest;
import com.example.dto.AuthResponse;

public interface AuthService {

    String register(RegisterRequest request);
    AuthResponse login(LoginRequest request);

}
