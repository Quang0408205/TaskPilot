package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String login() {

        System.out.println("===== LOGIN PAGE CALLED =====");

        return "login";
    }
}