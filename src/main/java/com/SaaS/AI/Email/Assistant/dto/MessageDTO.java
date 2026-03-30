package com.SaaS.AI.Email.Assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private String sender;
    private String content;
}
