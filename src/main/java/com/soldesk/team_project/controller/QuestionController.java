package com.soldesk.team_project.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.soldesk.team_project.dto.LawyerDTO;
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
    public String qnaList(@RequestParam(value = "page", defaultValue = "1") int page, Model model) {

        Page<QuestionDTO> paging = this.questionService.getQnaPaging(page);
        model.addAttribute("qnaPaging", paging);
        return "question/qnaList";
    }
    
    @GetMapping("/qnaWrite")
    public String qnaWrite(@ModelAttribute("qnaWrite") QuestionDTO qnaWrite){
        return "question/qnaWrite";
    }
    // @SessionAttribute("loginMember")MemberDTO loginMember,
    // @SessionAttribute("loginLawyer")LawyerDTO loginLawyer) {
    // qnaWrite.setMemberIdx(loginMember.getMemberIdx());
    // qnaWrite.setLawyerIdx(loginLawyer.getLawyerIdx());

    @PostMapping("/qnaWrite")
    public String qnaWriteSubmit(@ModelAttribute("qnaWrite") QuestionDTO qnaWrite) {
        questionService.qnaWriting(qnaWrite);
        return "question/qnaWrite";
    }

}
