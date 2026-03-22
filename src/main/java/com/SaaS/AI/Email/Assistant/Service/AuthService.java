package com.SaaS.AI.Email.Assistant.Service;

import com.SaaS.AI.Email.Assistant.Entity.User;
import com.SaaS.AI.Email.Assistant.Repository.UserRepo;
import com.SaaS.AI.Email.Assistant.dto.LoginRequest;
import com.SaaS.AI.Email.Assistant.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private BCryptPasswordEncoder PasswordEncoder;
    @Autowired
    private JwtService jwtUtil;

public void register(RegisterRequest request){
    if(userRepo.findByEmail(request.getEmail()).isPresent()){
        throw new RuntimeException("User Already Exists");
    }

    User user=User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(PasswordEncoder.encode(request.getPassword()))
            .createdAt(LocalDateTime.now())
            .build();

        userRepo.save(user);

    }


    public String login(LoginRequest request) {
        User user = userRepo.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        if (PasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            return jwtUtil.generateToken(user.getEmail());
        } else {
            throw new RuntimeException("Invalid credentials");
        }


    }


}
