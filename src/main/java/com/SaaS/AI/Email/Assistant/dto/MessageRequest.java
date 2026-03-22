package com.SaaS.AI.Email.Assistant.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private Long threadId;
    private String content;
}
