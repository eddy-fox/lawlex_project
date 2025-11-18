package com.soldesk.team_project.controller;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lawyer")
public class LawyerController {

    private final LawyerService lawyerService;
    private final MemberService memberService;
    private final InterestRepository interestRepository;

    // 변호사 회원가입 폼
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("lawyer", new LawyerDTO());
        model.addAttribute("interests", interestRepository.findAllByOrderByInterestNameAsc());
        return "member/ljoin";
    }

    // 변호사 회원가입 처리
    @PostMapping("/join")
    public String joinSubmit(@ModelAttribute("lawyer") LawyerDTO dto,
                             @RequestParam("certImage") MultipartFile certImage,
                             RedirectAttributes ra) {
        try {
            lawyerService.joinLawyer(dto, certImage);
            return "redirect:/member/login?joined=true";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/lawyer/join";
        }
    }

    // 아이디 중복 체크
    @GetMapping("/api/checkId")
    @ResponseBody
    public String checkId(@RequestParam("lawyerId") String lawyerId) {
        return memberService.isUserIdDuplicate(lawyerId) ? "DUP" : "OK";
    }
}
