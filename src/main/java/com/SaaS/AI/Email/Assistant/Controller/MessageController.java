package com.SaaS.AI.Email.Assistant.Controller;

import com.SaaS.AI.Email.Assistant.Entity.EmailThread;
import com.SaaS.AI.Email.Assistant.Entity.Message;
import com.SaaS.AI.Email.Assistant.Entity.User;
import com.SaaS.AI.Email.Assistant.Repository.EmailThreadRepo;
import com.SaaS.AI.Email.Assistant.Repository.MessageRepo;
import com.SaaS.AI.Email.Assistant.Repository.UserRepo;
import com.SaaS.AI.Email.Assistant.Service.AIService;
import com.SaaS.AI.Email.Assistant.dto.MessageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/threads/{threadId}")
public class MessageController {

    private final AIService aiService;
    private final MessageRepo messageRepo;
    private final EmailThreadRepo emailThreadRepo;
    private final UserRepo userRepo;

    public MessageController(AIService aiService, MessageRepo messageRepo, EmailThreadRepo emailThreadRepo, UserRepo userRepo) {
        this.aiService = aiService;
        this.messageRepo = messageRepo;
        this.emailThreadRepo = emailThreadRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/messages")
    public ResponseEntity<String> sendMessages(@PathVariable Long threadId, @RequestBody MessageRequest messageRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }

        String email = authentication.getName();

        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("User: " + email + ", UserId: " + user.getId() + ", ThreadId: " + threadId);


        EmailThread thread = emailThreadRepo.findByIdAndUser_Id(threadId, user.getId())
                .orElseThrow(() -> new RuntimeException("Thread not found or access denied"));


        Message userMessage = Message.builder()
                .sender("USER")
                .content(messageRequest.getContent())
                .createdAt(LocalDateTime.now())
                .thread(thread)
                .build();
        messageRepo.save(userMessage);


        String aiReply = aiService.sendMessage(messageRequest, email);


        Message aiMessage = Message.builder()
                .sender("AI")
                .content(aiReply)
                .createdAt(LocalDateTime.now())
                .thread(thread)
                .build();
        messageRepo.save(aiMessage);

        return ResponseEntity.status(201).body(aiReply);

    }
}
