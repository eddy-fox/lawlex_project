package com.soldesk.team_project.controller;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import com.soldesk.team_project.service.QuestionService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String qnaWrite() {
        return "question/qnaWrite";
    }

}
