package com.example.service;

import com.example.dto.RegisterRequest;
import com.example.dto.LoginRequest;

public interface AuthService {

    String register(RegisterRequest request);
    String login(LoginRequest request);

}