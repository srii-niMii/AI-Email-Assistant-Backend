package com.SaaS.AI.Email.Assistant.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "thread_id")
    private EmailThread thread;

}
