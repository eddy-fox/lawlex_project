package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.QuestionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    
    private final MemberService memberService;
    private final LawyerService lawyerService;
    private final QuestionService questionService;

    @GetMapping("/memberManagement")
    public String memberList(
        @RequestParam(value = "keyword",required = false) String keyword, Model model, 
        @RequestParam(value = "searchType", required = false, defaultValue = "idx") String searchType) {
        
        List<MemberDTO> memberList;

        if (keyword == null || keyword.trim().isEmpty()) {
            memberList = memberService.getAllMember();
        } else {
            memberList = memberService.searchMembers(searchType, keyword);
        }

        model.addAttribute("memberList", memberList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        return "admin/memberManagement";
    }
    @PostMapping("/memberManagement")
    public String memberSearch(
        @RequestParam String keyword, 
        @RequestParam String searchType,
        RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("searchType", searchType);
        return "redirect:/admin/memberManagement";
    }

    @GetMapping("/lawyerManagement")
    public String lawyerList(
        @RequestParam(value = "keyword",required = false) String keyword, Model model, 
        @RequestParam(value = "searchType", required = false, defaultValue = "idx") String searchType) {
        
        List<LawyerDTO> lawyerList;

        if (keyword == null || keyword.trim().isEmpty()) {
            lawyerList = lawyerService.getAllLawyer();
        } else {
            lawyerList = lawyerService.searchLawyers(searchType, keyword);
        }

        model.addAttribute("lawyerList", lawyerList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        return "admin/lawyerManagement";
    }
    @PostMapping("/lawyerManagement")
    public String lawyerSearch(
        @RequestParam String keyword, 
        @RequestParam String searchType,
        RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("searchType", searchType);
        return "redirect:/admin/lawyerManagement";
    }

    // @GetMapping("/QnAManagement")
    // public String questionList(
    //     @RequestParam(value = "keyword",required = false) String keyword, Model model, 
    //     @RequestParam(value = "searchType", required = false, defaultValue = "idx") String searchType) {
        
    //     List<QuestionDTO> newQuestions;
    //     List<QuestionDTO> completedQuestions;

    //     String qNew = "N";
    //     String qCompleted = "Y";


    //     if (keyword == null || keyword.trim().isEmpty()) {
    //         newQuestions = questionService.getNewQuestions(qNew);
    //         completedQuestions = questionService.getCompletedQuestions(qCompleted);
    //     } else {
    //         newQuestions = questionService.searchNewQuestions(searchType, keyword, qNew);
    //         completedQuestions = questionService.searchCompletedQuestions(searchType, keyword, qCompleted);
    //     }

    //     model.addAttribute("newQuestions", newQuestions);
    //     model.addAttribute("completedQuestions", completedQuestions);
    //     model.addAttribute("keyword", keyword);
    //     model.addAttribute("searchType", searchType);
    //     return "admin/QnAManagement";
    // }
    // @PostMapping("/QnAManagement")
    // public String questionSearch(
    //     @RequestParam String keyword, 
    //     @RequestParam String searchType,
    //     RedirectAttributes redirectAttributes) {

    //     redirectAttributes.addAttribute("keyword", keyword);
    //     redirectAttributes.addAttribute("searchType", searchType);
    //     return "redirect:/admin/QnAManagement";
    // }

}
