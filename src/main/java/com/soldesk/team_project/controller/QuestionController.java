package com.soldesk.team_project.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
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
    private final MemberService memberService;
    private final LawyerService lawyerService;

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
//     qnaWrite.setMemberIdx(loginMember.getMemberIdx());
//     qnaWrite.setLawyerIdx(loginLawyer.getLawyerIdx());

    @PostMapping("/qnaWrite")
    public String qnaWriteSubmit(@ModelAttribute("qnaWrite") QuestionDTO qnaWrite) {
        questionService.qnaWriting(qnaWrite);
        return "question/qnaWrite";
    }

    //     @GetMapping("/qnaInfo")
    // public String qnaAnswer(@RequestParam("qIdx") int qIdx, Model model) {
    //     QuestionDTO infoQ = questionService.getQ(qIdx);
    //     // if(infoQ == null) return "redirect:"; // null 이면 돌아가라
        
    //     Integer mIdx = infoQ.getMemberIdx();
    //     Integer lIdx = infoQ.getLawyerIdx();

    //     if (lIdx != null) {
    //         LawyerDTO l = lawyerService.qLawyerInquiry(lIdx);
    //         infoQ.setInfoId(l.getLawyerId());
    //         infoQ.setInfoName(l.getLawyerName());
    //     }else if (mIdx != null) {
    //         MemberDTO m = memberService.qMemberInquiry(mIdx);
    //         infoQ.setInfoId(m.getMemberId());
    //         infoQ.setInfoName(m.getMemberName());
    //     }
    //     model.addAttribute("infoQ", infoQ);
    //     return "admin/qnaInfo";
    // }

}
