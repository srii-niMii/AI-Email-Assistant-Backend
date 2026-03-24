package com.SaaS.AI.Email.Assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class AiEmailAssistantApplication {

	public static void main(String[] args) {

		SpringApplication.run(AiEmailAssistantApplication.class, args);
	}

}
