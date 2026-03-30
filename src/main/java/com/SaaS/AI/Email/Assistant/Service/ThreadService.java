package com.SaaS.AI.Email.Assistant.Service;

import com.SaaS.AI.Email.Assistant.Entity.EmailThread;
import com.SaaS.AI.Email.Assistant.Entity.User;
import com.SaaS.AI.Email.Assistant.dto.ThreadRequest;
import com.SaaS.AI.Email.Assistant.Repository.EmailThreadRepo;
import com.SaaS.AI.Email.Assistant.Repository.UserRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ThreadService {
    private EmailThreadRepo emailThreadRepo;
    private UserRepo userRepo;

    public ThreadService(EmailThreadRepo emailThreadRepo, UserRepo userRepo) {
        this.emailThreadRepo = emailThreadRepo;
        this.userRepo = userRepo;
    }


    public EmailThread createThread(ThreadRequest request, String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmailThread thread = EmailThread.builder()
                .title(request.getTitle())
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        return emailThreadRepo.save(thread);
    }


    public List<EmailThread> getThreads(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return emailThreadRepo.findByUser_Id(user.getId());
    }

    public EmailThread getThread(Long threadId, String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return emailThreadRepo.findByIdAndUser_Id(threadId, user.getId())
                .orElseThrow(() -> new RuntimeException("Thread not found or access denied"));
    }

    public void deleteThread(Long id, String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmailThread thread = emailThreadRepo.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Thread not found or access denied"));

        emailThreadRepo.delete(thread);

    }
}

