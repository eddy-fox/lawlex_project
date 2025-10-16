package com.soldesk.team_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TemporaryController {
        
    @GetMapping("/member/login")
    public String loginPage() {
        return "member/login";
    }
}
