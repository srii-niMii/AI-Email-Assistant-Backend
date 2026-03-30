package com.SaaS.AI.Email.Assistant.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String content;
    private String tone;
    private Long threadId;
}
