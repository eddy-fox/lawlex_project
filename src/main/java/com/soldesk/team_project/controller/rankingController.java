package com.soldesk.team_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/rank")
public class rankingController {
    
    @GetMapping("/ranking")
    public String ranking() {
        return "rank/ranking";
    }
    
}
