package com.SaaS.AI.Email.Assistant.Service;

import com.SaaS.AI.Email.Assistant.Entity.EmailThread;
import com.SaaS.AI.Email.Assistant.Entity.Message;
import com.SaaS.AI.Email.Assistant.Entity.User;
import com.SaaS.AI.Email.Assistant.Repository.EmailThreadRepo;
import com.SaaS.AI.Email.Assistant.Repository.MessageRepo;
import com.SaaS.AI.Email.Assistant.Repository.UserRepo;
import com.SaaS.AI.Email.Assistant.dto.MessageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private MessageRepo messageRepo;
    private EmailThreadRepo emailThreadRepo;
    private UserRepo userRepo;

    public MessageService(MessageRepo messageRepo, EmailThreadRepo emailThreadRepo, UserRepo userRepo) {
        this.messageRepo = messageRepo;
        this.emailThreadRepo = emailThreadRepo;
        this.userRepo = userRepo;
    }

    public void sendMessage(MessageRequest request){
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        EmailThread thread = emailThreadRepo.findById(request.getThreadId()).orElseThrow(() -> new RuntimeException("Thread not found"));

        if(!thread.getUser().getId().equals(user.getId())){
            throw new RuntimeException("Unauthorized");
        }

        Message message = Message.builder()
                .content(request.getContent())
                .sender("USER")
                .createdAt(LocalDateTime.now())
                .thread(thread)
                .build();
        messageRepo.save(message);
    }

    public List<Message> getMessages(Long threadId){
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        EmailThread thread = emailThreadRepo.findById(threadId).orElseThrow(() -> new RuntimeException("Thread not found"));

        if (!thread.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        return messageRepo.findByThreadId(threadId);
    }
}
