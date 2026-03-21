package com.SaaS.AI.Email.Assistant.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Home {

    @RequestMapping("/")
    public String main() {
        return ("Hello World");
    }
}
