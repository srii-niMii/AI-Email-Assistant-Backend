package com.SaaS.AI.Email.Assistant.Repository;

import com.SaaS.AI.Email.Assistant.Entity.EmailThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailThreadRepo extends JpaRepository<EmailThread, Long> {
    List<EmailThread> findByUserId(Long userId);
}

