package com.SaaS.AI.Email.Assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EmailThreadDTO {
    private Long id;
    private String title;
    private List<MessageDTO> messages;
}