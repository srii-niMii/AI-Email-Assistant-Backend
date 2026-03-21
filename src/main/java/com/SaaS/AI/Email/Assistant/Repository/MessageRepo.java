package com.SaaS.AI.Email.Assistant.Repository;

import com.SaaS.AI.Email.Assistant.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {
    List<Message> findByThreadId(Long threadId);
}
