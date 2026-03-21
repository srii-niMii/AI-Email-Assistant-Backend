package com.SaaS.AI.Email.Assistant.Repository;

import com.SaaS.AI.Email.Assistant.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}
