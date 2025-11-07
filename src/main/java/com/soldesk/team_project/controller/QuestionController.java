package com.soldesk.team_project.controller;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.service.QuestionService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/faq")
    public String faq() {
        return "question/faq";
    }

    @GetMapping("/qnaList")
    public String qnaList() {
        return "question/qnaList";
    }
    
    @GetMapping("/qnaWrite")
    public String qnaWrite(@ModelAttribute("qWrite") QuestionDTO qWrite) {
        
        return "question/qnaWrite";
    }

    @PostMapping("/qnaWrite")
    public String qnaWriteSubmit(@ModelAttribute("qWrite") QuestionDTO qWrite) {

        return "question/qnaWrite";
    }

}
