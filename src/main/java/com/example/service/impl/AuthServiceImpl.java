package com.example.service.impl;

import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public String register(
            RegisterRequest request){

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException(
                    "Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        userRepository.save(user);

        return "Register success";
    }

    @Override
    public String login(LoginRequest request){

        User user = userRepository.findByEmail(
                request.getEmail())
                .orElseThrow(() ->
                new RuntimeException("User not found"));

        if(!user.getPassword().equals(request.getPassword())){
            throw new RuntimeException(
                    "Wrong password");
        }

        return "Login success";
    }
}