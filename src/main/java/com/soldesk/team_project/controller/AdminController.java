package com.lawlex.project_test.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lawlex.project_test.dto.MemberDTO;
import com.lawlex.project_test.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    
    private final MemberService memberService;

    @GetMapping("/memberManagement")
    public String memberList(
        @RequestParam(required = false) String keyword, 
        @RequestParam("searchType") String type, Model model) {
        
        List<MemberDTO> memberList;

        if (keyword == null || keyword.trim().isEmpty()) {
            memberList = memberService.getAllMember();
        } else {
            memberList = memberService.searchMembers(type, keyword);
        }

        model.addAttribute("memberList", memberList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
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

}
