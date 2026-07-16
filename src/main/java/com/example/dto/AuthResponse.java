package com.example.dto;
public record AuthResponse(String token, String tokenType, long expiresIn) { }
