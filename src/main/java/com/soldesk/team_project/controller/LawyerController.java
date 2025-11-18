package com.soldesk.team_project.controller;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.service.CalendarService;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.RankingService;
// import com.soldesk.team_project.controller.MemberController.SessionUser;
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
    private final MemberService memberService;           // 아이디 중복 체크 재사용
    private final InterestRepository interestRepository; // 폼 선택항목
    private final LawyerRepository lawyerRepository;     // 마이페이지 뷰용 로딩
    private final CalendarService calendarService;       // 상담 가능 시간 체크용
    private final RankingService rankingService;         // 랭킹 계산용

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

    // 변호사 프로필 페이지
    @GetMapping("/profile/{lawyerIdx}")
    public String profile(@PathVariable("lawyerIdx") Integer lawyerIdx, Model model) {
        LawyerEntity lawyer = lawyerRepository.findById(lawyerIdx).orElse(null);
        if (lawyer == null || lawyer.getLawyerActive() == null || lawyer.getLawyerActive() != 1) {
            return "redirect:/";
        }

        // 변호사 정보 설정
        model.addAttribute("lawyer", lawyer);
        model.addAttribute("lawyerIdx", lawyerIdx);
        
        // 상담 가능 여부 체크 (30분, 60분)
        boolean canRequest30 = calendarService.canRequestNow(lawyerIdx, 30);
        boolean canRequest60 = calendarService.canRequestNow(lawyerIdx, 60);
        model.addAttribute("canRequest30", canRequest30);
        model.addAttribute("canRequest60", canRequest60);
        
        // 랭킹 계산
        int likeRanking = rankingService.getLikeRanking(lawyerIdx);
        int answerRanking = rankingService.getAnswerRanking(lawyerIdx);
        model.addAttribute("likeRanking", likeRanking);
        model.addAttribute("answerRanking", answerRanking);
        
        return "lawyer/profile";
    }
}
