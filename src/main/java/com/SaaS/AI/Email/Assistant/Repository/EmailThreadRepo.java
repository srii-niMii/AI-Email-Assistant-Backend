package com.SaaS.AI.Email.Assistant.Repository;

import com.SaaS.AI.Email.Assistant.Entity.EmailThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailThreadRepo extends JpaRepository<EmailThread, Long> {
    List<EmailThread> findByUser_Id(Long id);
    Optional<EmailThread> findByIdAndUser_Id(Long id, Long userId);
}

