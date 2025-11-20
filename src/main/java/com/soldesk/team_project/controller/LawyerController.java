package com.soldesk.team_project.controller;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.service.CalendarService;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.RankingService;
// import com.soldesk.team_project.controller.MemberController.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

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
                             @RequestParam(value = "lawyerImage", required = false) MultipartFile lawyerImage,
                             @RequestParam(value = "availabilityJson", required = false) String availabilityJson,
                             RedirectAttributes ra) {
        try {
            lawyerService.joinLawyer(dto, certImage, lawyerImage, availabilityJson);
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

    // ===== 변호사 정보 수정 API =====

    // 변호사 프로필 수정
    @PostMapping(value="/api/profile", produces="text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> updateProfileForLawyer(
            @SessionAttribute(value="loginUser", required = false) UserMasterDTO loginUser,
            @ModelAttribute LawyerDTO form,
            @RequestParam(value="lawyerImage", required = false) MultipartFile lawyerImage,
            @RequestParam(value="calendarJson", required = false) String calendarJson) {

        if (loginUser == null || !"LAWYER".equalsIgnoreCase(loginUser.getRole())) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        try {
            lawyerService.updateProfileForCurrent(form, lawyerImage, calendarJson);
            return ResponseEntity.ok("OK");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SERVER_ERROR");
        }
    }

    // 변호사 비밀번호 변경(아이디+전화번호+생년월일 검증)
    @PostMapping(value="/api/changePassword", produces="text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> changePasswordForLawyer(
            @SessionAttribute(value="loginUser", required = false) UserMasterDTO loginUser,
            @RequestParam("lawyerId") String lawyerId,
            @RequestParam("lawyerPhone") String lawyerPhone,
            @RequestParam("lawyerIdnum")  String lawyerIdnum,
            @RequestParam("newPassword")  String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {

        if (loginUser == null || !"LAWYER".equalsIgnoreCase(loginUser.getRole())) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String res = lawyerService.changePasswordWithVerificationForCurrent(lawyerId, lawyerPhone, lawyerIdnum, newPassword, confirmPassword);
        return switch (res) {
            case "OK"       -> ResponseEntity.ok("OK");
            case "MISMATCH" -> ResponseEntity.badRequest().body("비밀번호 확인이 일치하지 않습니다.");
            default         -> ResponseEntity.badRequest().body("본인 확인에 실패했습니다.");
        };
    }

    // 변호사 회원 탈퇴(전화번호+생년월일 검증)
    @PostMapping(value="/api/deactivate", produces="text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> deactivateLawyer(
            @SessionAttribute(value="loginUser", required = false) UserMasterDTO loginUser,
            HttpSession session,
            @RequestParam("lawyerPhone") String lawyerPhone,
            @RequestParam("lawyerIdnum")  String lawyerIdnum) {

        if (loginUser == null || !"LAWYER".equalsIgnoreCase(loginUser.getRole())) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        try {
            boolean ok = lawyerService.deactivateWithVerificationForCurrent(lawyerPhone, lawyerIdnum);
            if (ok) {
                session.invalidate();
                return ResponseEntity.ok("OK");
            }
            return ResponseEntity.badRequest().body("본인 확인에 실패했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SERVER_ERROR");
        }
    }
}
