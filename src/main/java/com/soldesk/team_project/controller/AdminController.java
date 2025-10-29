package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.soldesk.team_project.dto.AdDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.service.AdService;
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
    private final AdService adService;

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

    @GetMapping("/QnAManagement")
    public String questionList(
        @RequestParam(value = "keyword",required = false) String keyword, Model model, 
        @RequestParam(value = "searchType", required = false, defaultValue = "idx") String searchType) {
        
        List<QuestionDTO> newQuestions;
        List<QuestionDTO> completedQuestions;

        if (keyword == null || keyword.trim().isEmpty()) {
            newQuestions = questionService.getQuestions(0);
            completedQuestions = questionService.getQuestions(1);
        } else {
            newQuestions = questionService.searchQuestions(searchType, keyword, 0);
            completedQuestions = questionService.searchQuestions(searchType, keyword, 1);
        }

        model.addAttribute("newQuestions", newQuestions);
        model.addAttribute("completedQuestions", completedQuestions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        return "admin/QnAManagement";
    }
    @PostMapping("/QnAManagement")
    public String questionSearch(
        @RequestParam String keyword, 
        @RequestParam String searchType,
        RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("searchType", searchType);
        return "redirect:/admin/QnAManagement";
    }

    @GetMapping("/adManagement")
    public String adList(Model model) {
        
        // 만료된 광고 비활성화
        adService.refreshActiveAds();

        // 활성 광고 조회
        List<AdDTO> adList = adService.getAllAd();

        model.addAttribute("adList", adList);
        return "admin/adManagement";
    }

    @GetMapping("/adRegistration")
    public String registAdForm(@ModelAttribute("adRegistration")AdDTO adRegistration) {
        return "admin/adRegistration";
    }
    @PostMapping("/adRegistration")
    public String registAdSubmit(@ModelAttribute("adRegistration")AdDTO adRegistration) {
        adService.registProcess(adRegistration);

        return "redirect:/admin/adManagement";
    }

    @GetMapping("/adInfo")
    public String showAd(@RequestParam("adIdx") int adIdx, Model model) {
        AdDTO ad = adService.getAd(adIdx);
        model.addAttribute("ad", ad);

        return "admin/adInfo";
    }

    @GetMapping("/adModify")
    public String modifyForm(@RequestParam("adIdx") int adIdx, Model model) {
        AdDTO modifyAd = adService.getAd(adIdx);
        model.addAttribute("modifyAd", modifyAd);

        return "admin/adInfo";
    }
    @PostMapping("/adModify")
    public String modifySubmit(@ModelAttribute("modifyAd") AdDTO modifyAd, Model model) {
        adService.modifyProcess(modifyAd);

        return "redirect:/admin/adInfo?adIdx";
    }

    @GetMapping("/adDelete")
    public String deleteAd(@RequestParam("adIdx") int adIdx) {
        adService.deleteProcess(adIdx);

        return "admin/adInfo";
    }
    
}
