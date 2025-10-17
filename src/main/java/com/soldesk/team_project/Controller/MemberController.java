package com.soldeskp.p2.Controller;

import com.soldeskp.p2.DTO.MemberDTO;
import com.soldeskp.p2.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    // 회원가입 페이지 이동
    @GetMapping("/join")
    public String joinForm() {
        return "Member/join"; //
    }
//20251017
    // 회원가입 처리
    @PostMapping("/join")
    public String join(@ModelAttribute MemberDTO member, Model model) {
        boolean success = memberService.joinMember(member);

        if (success) {
            return "redirect:/member/login"; // 성공 시 로그인 페이지로 이동
        } else {
            model.addAttribute("error", "회원가입에 실패했습니다.");
            return "Member/join";
        }
    }

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String loginForm() {
        return "Member/login";
    }

    // 아이디 중복확인 (AJAX)
    @ResponseBody
    @GetMapping("/checkId")
    public String checkId(@RequestParam("memberId") String memberId) {
        boolean exists = memberService.checkDuplicateId(memberId);
        return exists ? "duplicate" : "available";
    }

}
