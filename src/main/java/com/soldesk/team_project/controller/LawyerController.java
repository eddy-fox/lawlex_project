package com.soldesk.team_project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.service.LawyerService;

import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lawyer")
public class LawyerController {

    private final LawyerService lawyerService;
    private final LawyerRepository lawyerRepository;
    private final InterestRepository interestRepository; // ★ 추가

    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("lawyer", new LawyerDTO());
        model.addAttribute("interests", interestRepository.findAllByOrderByInterestNameAsc()); // ★
        return "lawyer/join";
    }

    @PostMapping("/join/submit")
    public String joinSubmit(@ModelAttribute("lawyer") @Valid LawyerDTO lawyer,
                             BindingResult br,
                             Model model) {
        if (lawyer.getIdImage()==null || lawyer.getIdImage().isEmpty() ||
            lawyer.getCertImage()==null || lawyer.getCertImage().isEmpty()) {
            br.rejectValue("idImage","required","신분증과 변호사등록증 이미지는 필수입니다.");
        }
        if (br.hasErrors()) {
            model.addAttribute("interests", interestRepository.findAllByOrderByInterestNameAsc());
            return "lawyer/join";
        }
        try {
            lawyerService.register(lawyer);
            model.addAttribute("msg","신청이 접수되었습니다. 관리자 승인 후 이용 가능합니다.");
        } catch (Exception e){
            model.addAttribute("msg","업로드 중 오류: "+e.getMessage());
        }
        model.addAttribute("interests", interestRepository.findAllByOrderByInterestNameAsc());
        return "lawyer/join";
    }

    @GetMapping("/mypage")
    public String mypage(@RequestParam("idx") Integer idx, Model model) {
        LawyerEntity me = lawyerRepository.findById(idx).orElse(null);
        model.addAttribute("me", me);
        return "lawyer/mypage";
    }

    @PostMapping("/update")
    public String update(@RequestParam("idx") Integer idx,
                         @RequestParam(value="lawyerAddress", required=false) String addr,
                         @RequestParam(value="lawyerTel", required=false) String tel,
                         @RequestParam(value="lawyerComment", required=false) String comment) {
        LawyerDTO dto = LawyerDTO.builder()
                .lawyerAddress(addr).lawyerTel(tel).lawyerComment(comment).build();
        lawyerService.updateProfile(idx, dto);
        return "redirect:/lawyer/mypage?idx="+idx;
    }

    // ★ 아이디 중복체크 (일반회원과 동일 UX)
    @GetMapping("/api/checkId")
    @ResponseBody
    public String checkId(@RequestParam String lawyerId){
        return lawyerRepository.findByLawyerId(lawyerId).isPresent() ? "DUP" : "OK";
    }
}
