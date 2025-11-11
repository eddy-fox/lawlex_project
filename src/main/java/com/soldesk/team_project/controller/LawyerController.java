package com.soldesk.team_project.controller;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
// import com.soldesk.team_project.controller.MemberController.SessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lawyer")
public class LawyerController {

    private final LawyerService lawyerService;
    private final MemberService memberService;           // 아이디 중복 체크 재사용
    private final InterestRepository interestRepository; // 폼 선택항목
    private final LawyerRepository lawyerRepository;     // 마이페이지 뷰용 로딩

    // 회원가입 
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("lawyer", new LawyerDTO());
        model.addAttribute("interests", interestRepository.findAllByOrderByInterestNameAsc());
        return "lawyer/join";
    }

    // 회원가입 처리 (서비스 호출)
    @PostMapping("/join/submit")
    public String joinSubmit(@ModelAttribute("lawyer") LawyerDTO dto,
                             RedirectAttributes ra) {
        try {
            lawyerService.joinFromPortal(dto);
            return "redirect:/member/login?joined";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/lawyer/join";
        }
    }

    // 마이페이지 (세션의 LAWYER만 접근)
    // @GetMapping("/mypage")
    // public String mypage(@SessionAttribute(value = "loginUser", required = false) SessionUser loginUser,
    //                      Model model) {
    //     if (loginUser == null || !"LAWYER".equalsIgnoreCase(loginUser.role) || loginUser.lawyerIdx == null) {
    //         return "redirect:/member/login";
    //     }
    //     LawyerEntity me = lawyerRepository.findById(loginUser.lawyerIdx).orElse(null);
    //     if (me == null) return "redirect:/member/login";

    //     model.addAttribute("me", me);
    //     model.addAttribute("loginUser", loginUser);
    //     model.addAttribute("interests", interestRepository.findAllByOrderByInterestNameAsc());
    //     return "lawyer/mypage";
    // }

    // // 프로필 수정 (서비스 호출)
    // @PostMapping("/update")
    // public String update(@ModelAttribute LawyerDTO dto,
    //                      @RequestParam(required = false) String newPassword,
    //                      @RequestParam(required = false) String confirmPassword,
    //                      @SessionAttribute(value = "loginUser", required = false) SessionUser loginUser,
    //                      HttpSession session,
    //                      RedirectAttributes ra) {
    //     if (loginUser == null || !"LAWYER".equalsIgnoreCase(loginUser.role) || loginUser.lawyerIdx == null) {
    //         return "redirect:/member/login";
    //     }
    //     try {
    //         var result = lawyerService.updateProfileFromPortal(dto, newPassword, confirmPassword,
    //                                                            loginUser.userIdx, loginUser.lawyerIdx);

    //         // 세션의 userId만 갱신  나머지 세부 필드는 /member 쪽에서 이미 처리 패턴 존재
    //         loginUser.userId = result.newUserId();
    //         session.setAttribute("loginUser", loginUser);

    //         ra.addFlashAttribute("updated", true);
    //         return "redirect:/member/mypage"; 
    //     } catch (IllegalArgumentException e) {
    //         ra.addFlashAttribute("error", e.getMessage());
    //         return "redirect:/member/mypage";
    //     }
    // }

    // 아이디 중복 체크 (공통 로직 재사용)
    @GetMapping("/api/checkId")
    @ResponseBody
    public String checkId(@RequestParam String lawyerId) {
        return memberService.isUserIdDuplicate(lawyerId) ? "DUP" : "OK";
    }
}
