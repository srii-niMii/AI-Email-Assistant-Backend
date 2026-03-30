package com.SaaS.AI.Email.Assistant.Controller;

import com.SaaS.AI.Email.Assistant.Entity.EmailThread;
import com.SaaS.AI.Email.Assistant.Entity.User;
import com.SaaS.AI.Email.Assistant.Repository.UserRepo;
import com.SaaS.AI.Email.Assistant.Service.ThreadService;
import com.SaaS.AI.Email.Assistant.dto.EmailThreadDTO;
import com.SaaS.AI.Email.Assistant.dto.MessageDTO;
import com.SaaS.AI.Email.Assistant.dto.ThreadRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/threads")
public class ThreadController {

    public ThreadService threadService;
    private final UserRepo userRepo;

    public ThreadController(ThreadService threadService, UserRepo userRepo) {
        this.threadService = threadService;
        this.userRepo = userRepo;
    }

//    @PostMapping
//    public ResponseEntity createThread(@RequestBody ThreadRequest threadRequest) {
//        threadService.createThread(threadRequest);
//        return ResponseEntity.status(201).body("Thread created successfully");
//    }

    @PostMapping
    public ResponseEntity createThread(@RequestBody ThreadRequest threadRequest,
                                       Authentication authentication) {

        String email = authentication.getName();
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        EmailThread savedThread = threadService.createThread(threadRequest, user.getEmail());

        // Map to DTO to avoid recursion
        EmailThreadDTO dto = new EmailThreadDTO(
                savedThread.getId(),
                savedThread.getTitle(),
                List.of()
        );


        return ResponseEntity.status(201).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<EmailThreadDTO>> getThreads(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<EmailThread> threads = threadService.getThreads(user.getEmail());

        List<EmailThreadDTO> threadDTOs = threads.stream()
                .map(t -> new EmailThreadDTO(
                        t.getId(),
                        t.getTitle(),
                        t.getMessages().stream()
                                .map(m -> new MessageDTO(m.getId(), m.getSender(), m.getContent()))
                                .toList()
                ))
                .toList();

        return ResponseEntity.ok(threadDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailThreadDTO> getThread(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmailThread t = threadService.getThread(id, user.getEmail());

        EmailThreadDTO threadDTO = new EmailThreadDTO(
                t.getId(),
                t.getTitle(),
                t.getMessages().stream()
                        .map(m -> new MessageDTO(m.getId(), m.getSender(), m.getContent()))
                        .toList()
        );

        return ResponseEntity.ok(threadDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteThread(@PathVariable Long id, Authentication authentication) {

        String email = authentication.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        threadService.deleteThread(id, user.getEmail());

        return ResponseEntity.ok("Thread deleted successfully");
    }
}
