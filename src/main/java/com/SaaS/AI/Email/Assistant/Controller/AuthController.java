package com.SaaS.AI.Email.Assistant.Controller;

import com.SaaS.AI.Email.Assistant.Service.AuthService;
import com.SaaS.AI.Email.Assistant.dto.AuthResponse;
import com.SaaS.AI.Email.Assistant.dto.LoginRequest;
import com.SaaS.AI.Email.Assistant.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService=authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request)
    {
        authService.register(request);
        return ResponseEntity.status(201).body("User registered successfully");
    }

        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    }

