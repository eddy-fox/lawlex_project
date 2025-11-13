package com.soldesk.team_project.controller;

import java.util.zip.Inflater;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.QuestionService;

import jakarta.servlet.http.HttpSession;
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
    public String qnaList(@RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "mine", defaultValue = "false" ) boolean mine,
                          @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                            Model model) {

        Page<QuestionDTO> paging;

        if(mine && loginUser != null){
            Integer mIdx = loginUser.getMemberIdx();
            Integer lIdx = loginUser.getLawyerIdx();
            if(mine && mIdx!= null){ /* 일반회원이 확인 */
                paging = questionService.getQnaPagingM(mIdx, page);
            }else if(mine && lIdx != null){ /* 변호사회원 확인 */
                paging = questionService.getQnaPagingL(lIdx, page);
            }else {
                paging = questionService.getQnaPaging(page);
            }
        }else{ paging = questionService.getQnaPaging(page); }

        model.addAttribute("qnaPaging", paging);
        model.addAttribute("mine", mine);
       
        if(loginUser != null){
            model.addAttribute("loginUser", loginUser);
            if(loginUser.getMemberIdx() != null){
                model.addAttribute("myIdxM", loginUser.getMemberIdx());
            }
            if(loginUser.getLawyerIdx() != null) {
                model.addAttribute("myIdxL", loginUser.getLawyerIdx());
            }
        }
        return "question/qnaList";
    }
    
    @GetMapping("/qnaWrite")
    public String qnaWrite(@ModelAttribute("qnaWrite") QuestionDTO qnaWrite,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser){

        if(loginUser == null){ return "redirect:/member/login"; }
        System.out.println("\n"+ qnaWrite.toString() + "\n");
        return "question/qnaWrite";
    }
    
    @PostMapping("/qnaWrite")
    public String qnaWriteSubmit(@ModelAttribute("qnaWrite") QuestionDTO qnaWrite,
                                @SessionAttribute("loginUser") UserMasterDTO loginUser) {
        
        if(loginUser.getMemberIdx() != null ) {
            qnaWrite.setMemberIdx(loginUser.getMemberIdx());
        }
        if(loginUser.getLawyerIdx() != null) {
            qnaWrite.setLawyerIdx(loginUser.getLawyerIdx());
        }

        questionService.qnaWriting(qnaWrite);
        System.out.println("\n"+ qnaWrite.toString() + "\n");
        return "redirect:/question/qnaList";
    }

    @GetMapping("/qnaInfo")
    public String qnaInfo(@RequestParam("qIdx") int qIdx, Model model) {
        QuestionDTO infoQ = questionService.getQ(qIdx);

        

        Integer mIdx = infoQ.getMemberIdx();
        Integer lIdx = infoQ.getLawyerIdx();

        if (lIdx != null) {
            LawyerDTO l = lawyerService.qLawyerInquiry(lIdx);
            infoQ.setInfoId(l.getLawyerId());
            infoQ.setInfoName(l.getLawyerName());
        }else if (mIdx != null) {
            MemberDTO m = memberService.qMemberInquiry(mIdx);
            infoQ.setInfoId(m.getMemberId());
            infoQ.setInfoName(m.getMemberName());
        }
        model.addAttribute("infoQ", infoQ);
        return "question/qnaInfo";
    }

}
