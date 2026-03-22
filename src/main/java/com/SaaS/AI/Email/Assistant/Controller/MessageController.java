package com.SaaS.AI.Email.Assistant.Controller;

import com.SaaS.AI.Email.Assistant.Entity.Message;
import com.SaaS.AI.Email.Assistant.Service.MessageService;
import com.SaaS.AI.Email.Assistant.dto.MessageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody MessageRequest messageRequest) {
        messageService.sendMessage(messageRequest);
        return ResponseEntity.status(201).body("Message sent successfully");
    }

    @GetMapping("/{threadId}")
    public ResponseEntity <List<Message>> getMessages(@PathVariable Long threadId) {
        List<Message> messages = messageService.getMessages(threadId);
        return ResponseEntity.ok(messages);
}
}
