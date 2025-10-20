package com.soldesk.team_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping("question")
public class QuestionController {

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }
    @GetMapping("/qa")
    public String qa() {
        return "qa";
    }
    
    
}
