package com.soldesk.team_project.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soldesk.team_project.dto.AdDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.PointDTO;
import com.soldesk.team_project.dto.ProductDTO;
import com.soldesk.team_project.dto.PurchaseDTO;
import com.soldesk.team_project.dto.TemporaryOauthDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.security.JwtProvider;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.PurchaseService;
import com.soldesk.team_project.service.PythonService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.entity.*;
import com.soldesk.team_project.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.soldesk.team_project.entity.AdminEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.UserMasterEntity;



@Controller
@RequestMapping("member")
@RequiredArgsConstructor
public class MemberController {

    private final PurchaseService purchaseService;
    private final PythonService pythonService;

    private final MemberService memberService;
    private final LawyerService lawyerService;

    private final UserMasterRepository userMasterRepository;
    private final MemberRepository memberRepository;
    private final LawyerRepository lawyerRepository;
    private final AdminRepository adminRepository;
    private final InterestRepository interestRepository; // 로이어 관심 1개를 조인으로 세팅할 때 사용
    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    // 멤버 포인트
    @GetMapping("/point")
    public String pointMain(Model model, @SessionAttribute("loginUser") UserMasterDTO loginUser) {
        
        Integer memberIdx = loginUser.getMemberIdx();
                
        // 포인트 구매 상품
        List<ProductDTO> productList = purchaseService.getBuyPointProduct();
        model.addAttribute("productList", productList);
        
        // 포인트 사용 내역 및 결제 내역
        List<PointDTO> pointList = purchaseService.getAllPoint(memberIdx);
        List<PurchaseDTO> purchaseList = purchaseService.getAllPurchase(memberIdx);

        model.addAttribute("pointList", pointList);
        model.addAttribute("purchaseList", purchaseList);
        
        return "member/point";
    }
    @PostMapping("/point")
    public String productPurchase(
        @RequestParam("selectedProduct") int productNum, Model model,
        @SessionAttribute("loginUser") UserMasterDTO loginUser) {

        Integer memberIdx = loginUser.getMemberIdx();
        String memberIdxStr = String.valueOf(memberIdx);
        List<MemberDTO> member = memberService.searchMembers("Idx", memberIdxStr);
        model.addAttribute("member", member);

        // 구매 검증을 위한 ID 생성
        String purchaseId = "order-" + System.currentTimeMillis();
        // 구매 요청 내역 생성
        PurchaseDTO purchase = purchaseService.createPendingPurchase(productNum, purchaseId, memberIdx);
        model.addAttribute("purchase", purchase);
        
        return "payment/checkout";
    }

    @GetMapping("/testOCR") 
    public String testOCR(Model model) {

        LawyerDTO lawyerDTO = new LawyerDTO();
        lawyerDTO.setLawyerAuth(0);
        model.addAttribute("lawyerAuth", new LawyerDTO());

        return "member/testOCR";
    }
    @PostMapping("/verify-license")
    @ResponseBody
    public Map<String, Object> verifyLicense(
        @RequestParam("licenseNumber") String licenseNumber,
        @RequestParam("licenseImage") MultipartFile licenseImage) {

        Map<String, Object> result = new HashMap<>();     

        try {
            // 1. 업로드된 파일을 임시 경로에 저장
            File tempFile = File.createTempFile("license_", ".jpg");
            licenseImage.transferTo(tempFile);

            // 2. OCR 스크립트 실행
            Map<String, Object> ocrResult = pythonService.runPythonOCR("ocr.py", tempFile.toString());
            if (!(boolean) ocrResult.getOrDefault("valid", false)) {
                result.put("valid", false);
                result.put("error", ocrResult.get("error"));
                return result;
            }

            // 3. 출력값과 일치하는지 확인
            List<String> ocrTexts = (List<String>) ocrResult.get("texts");
            boolean matched = ocrTexts.stream().anyMatch(text -> text.contains(licenseNumber));

            // 4. 결과 전달, 임시파일 삭제
            result.put("valid", matched);
            result.put("message", matched ? "자격번호 일치!" : "자격번호 불일치!");
            result.put("ocrTexts", ocrTexts); // 테스트할때만 출력
            tempFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
            result.put("valid", false);
            result.put("error", "검증 과정 중 오류 발생: " + e.getMessage());
        }

        return result;
    }
    
    // 로그인 / 로그아웃
    @GetMapping("/login")
    public String loginForm() { return "member/login"; }

    @PostMapping("/login")
public String loginSubmit(@ModelAttribute MemberDTO loginConfirmMember,
                          HttpSession session) {

    String userId = loginConfirmMember.getMemberId();
    String rawPw  = loginConfirmMember.getMemberPass();

    System.out.println("=== LOGIN TRY ===");
    System.out.println("userId = " + userId);
    System.out.println("rawPw  = " + rawPw);

    // 1) 먼저 user_master에서 통합 계정 찾기
    var uOpt = userMasterRepository.findByUserId(userId);
    if (uOpt.isPresent()) {
        var u = uOpt.get();
        String dbPw = u.getPassword();
        boolean matched = false;

        // 암호화된 경우
        if (dbPw != null) {
            matched = passwordEncoder.matches(rawPw, dbPw);
            // 더미처럼 평문 저장된 경우도 허용
            if (!matched && rawPw.equals(dbPw)) {
                matched = true;
            }
        }

        if (!matched) {
            System.out.println("user_master password not matched");
            return "redirect:/member/login?error";
        }

        // 여기부터는 원래 세션 세팅 로직
        String role = u.getRole() == null ? "" : u.getRole().toUpperCase();
        SessionUser su = new SessionUser(
                u.getUserIdx(),
                u.getUserId(),
                role,
                u.getMemberIdx(),
                u.getLawyerIdx(),
                u.getAdminIdx()
        );
        session.setAttribute("loginUser", su);

        // 역할별 세션 정보 채우기
        switch (role) {
            case "MEMBER" -> {
                if (u.getMemberIdx() != null) {
                    memberRepository.findById(u.getMemberIdx()).ifPresent(me -> {
                        MemberSession ms = new MemberSession(
                                me.getMemberIdx(), me.getMemberId(), me.getMemberName(),
                                me.getMemberEmail(), me.getMemberPhone(), me.getMemberNickname(),
                                me.getInterestIdx1(), me.getInterestIdx2(), me.getInterestIdx3()
                        );
                        session.setAttribute("loginMember", ms);
                    });
                }
            }
            case "LAWYER" -> {
                if (u.getLawyerIdx() != null) {
                    lawyerRepository.findById(u.getLawyerIdx()).ifPresent(le -> {
                        LawyerSession ls = new LawyerSession(
                                le.getLawyerIdx(), le.getLawyerId(), le.getLawyerName(),
                                le.getLawyerEmail(), le.getLawyerPhone(), le.getInterestIdx()
                        );
                        session.setAttribute("loginLawyer", ls);
                    });
                }
            }
            case "ADMIN" -> {
                if (u.getAdminIdx() != null) {
                    adminRepository.findById(u.getAdminIdx()).ifPresent(ad -> {
                        AdminSession as = new AdminSession(
                                ad.getAdminIdx(), ad.getAdminId(), ad.getAdminName(),
                                ad.getAdminEmail(), ad.getAdminPhone(), ad.getAdminRole()
                        );
                        session.setAttribute("loginAdmin", as);
                    });
                }
            }
        }

        session.setMaxInactiveInterval(60 * 60); // 1시간
        System.out.println("=== LOGIN OK via user_master ===");
        return "redirect:/";
    }

    // 2) user_master에 없으면 member 테이블에서 직접 로그인 (더미용)
    var mOpt = memberRepository.findByMemberId(userId);
    if (mOpt.isPresent()) {
        var m = mOpt.get();
        String dbPw = m.getMemberPass();
        if (dbPw != null && dbPw.equals(rawPw)) {
            // 세션에 공통 유저 정보처럼 넣기
            SessionUser su = new SessionUser(
                    null,
                    m.getMemberId(),
                    "MEMBER",
                    m.getMemberIdx(),
                    null,
                    null
            );
            session.setAttribute("loginUser", su);

            MemberSession ms = new MemberSession(
                    m.getMemberIdx(), m.getMemberId(), m.getMemberName(),
                    m.getMemberEmail(), m.getMemberPhone(), m.getMemberNickname(),
                    m.getInterestIdx1(), m.getInterestIdx2(), m.getInterestIdx3()
            );
            session.setAttribute("loginMember", ms);

            session.setMaxInactiveInterval(60 * 60);
            System.out.println("=== LOGIN OK via member table ===");
            return "redirect:/";
        } else {
            System.out.println("member table password not matched");
            return "redirect:/member/login?error";
        }
    }

    // 3) 혹시 변호사 테이블만 있는 경우도 대비
    var lOpt = lawyerRepository.findByLawyerId(userId);
    if (lOpt.isPresent()) {
        var l = lOpt.get();
        String dbPw = l.getLawyerPass();
        if (dbPw != null && dbPw.equals(rawPw)) {
            SessionUser su = new SessionUser(
                    null,
                    l.getLawyerId(),
                    "LAWYER",
                    null,
                    l.getLawyerIdx(),
                    null
            );
            session.setAttribute("loginUser", su);

            LawyerSession ls = new LawyerSession(
                    l.getLawyerIdx(), l.getLawyerId(), l.getLawyerName(),
                    l.getLawyerEmail(), l.getLawyerPhone(), l.getInterestIdx()
            );
            session.setAttribute("loginLawyer", ls);

            session.setMaxInactiveInterval(60 * 60);
            System.out.println("=== LOGIN OK via lawyer table ===");
            return "redirect:/";
        } else {
            System.out.println("lawyer table password not matched");
            return "redirect:/member/login?error";
        }
    }

    // 4) 관리자 테이블만 있는 경우
    var aOpt = adminRepository.findByAdminId(userId);
    if (aOpt.isPresent()) {
        var a = aOpt.get();
        String dbPw = a.getAdminPass();
        if (dbPw != null && dbPw.equals(rawPw)) {
            SessionUser su = new SessionUser(
                    null,
                    a.getAdminId(),
                    "ADMIN",
                    null,
                    null,
                    a.getAdminIdx()
            );
            session.setAttribute("loginUser", su);

            AdminSession as = new AdminSession(
                    a.getAdminIdx(), a.getAdminId(), a.getAdminName(),
                    a.getAdminEmail(), a.getAdminPhone(), a.getAdminRole()
            );
            session.setAttribute("loginAdmin", as);

            session.setMaxInactiveInterval(60 * 60);
            System.out.println("=== LOGIN OK via admin table ===");
            return "redirect:/";
        } else {
            System.out.println("admin table password not matched");
            return "redirect:/member/login?error";
        }
    }

    // 여기까지 왔으면 어떤 테이블에서도 못 찾은 것
    System.out.println("user not found in any table");
    return "redirect:/member/login?error";
}



    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/member/login";
    }

    // 회원가입 페이지 + 마이페이지
    @GetMapping("/join/type")
    public String joinType() { return "member/joinType"; }

    @GetMapping({"/join/normal", "/joinNormal"})
    public String joinNormalForm() { return "member/joinNormal"; }

    @GetMapping({"/join/lawyer", "/lawyer/join"})
    public String joinLawyerForm() { return "lawyer/join"; }

    @GetMapping("/mypage")
    public String mypage(@SessionAttribute(value = "loginUser", required = false) SessionUser loginUser,
                         Model model) {
        if (loginUser == null) return "redirect:/member/login";
        model.addAttribute("loginUser", loginUser);
        return switch (loginUser.role) {
            case "MEMBER" -> "member/mypage";
            case "LAWYER" -> "lawyer/mypage";
            case "ADMIN"  -> "admin/mypage";
            default -> "redirect:/member/login";
        };
    }

    // ===== 공통 API =====
    @GetMapping("/api/checkId")
    @ResponseBody
    public String checkId(@RequestParam String memberId) {
        return memberService.isUserIdDuplicate(memberId) ? "DUP" : "OK";
    }

    // 일반 회원가입 → MemberService
    @PostMapping("/api/join/normal")
    @Transactional
    public String joinNormal(@ModelAttribute MemberDTO dto, RedirectAttributes ra) {
        try {
            memberService.joinNormal(dto);
            return "redirect:/member/login?joined";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/join/normal";
        }
    }

    // 변호사 회원가입 → LawyerService
    @PostMapping("/api/join/lawyer")
    @Transactional
    public String joinLawyer(@ModelAttribute LawyerDTO dto, RedirectAttributes ra) {
        try {
            lawyerService.joinFromPortal(dto);
            return "redirect:/member/login?joined";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/join/lawyer";
        }
    }

    // 일반회원 프로필 수정 → MemberService
    @PostMapping("/api/updateProfile")
    @Transactional
    public String updateProfileMember(@ModelAttribute MemberDTO dto,
                                      @RequestParam(required = false) String newPassword,
                                      @RequestParam(required = false) String confirmPassword,
                                      @SessionAttribute(value = "loginUser", required = false) SessionUser loginUser,
                                      HttpSession session,
                                      RedirectAttributes ra) {
        if (loginUser == null || !"MEMBER".equalsIgnoreCase(loginUser.role)) {
            return "redirect:/member/login";
        }
        if (loginUser.memberIdx == null ||
           (dto.getMemberIdx() != null && !loginUser.memberIdx.equals(dto.getMemberIdx()))) {
            return "redirect:/member/login";
        }

        try {
            var result = memberService.updateMemberProfile(dto, newPassword, confirmPassword,
                                                           loginUser.userIdx, loginUser.memberIdx);

            // 세션 갱신
            loginUser.userId = result.newUserId();
            session.setAttribute("loginUser", loginUser);

            Object msObj = session.getAttribute("loginMember");
            if (msObj instanceof MemberSession ms) {
                MemberEntity me = result.member();
                ms.memberId = me.getMemberId();
                ms.memberEmail = me.getMemberEmail();
                ms.memberNickname = me.getMemberNickname();
                ms.interestIdx1 = me.getInterestIdx1();
                ms.interestIdx2 = me.getInterestIdx2();
                ms.interestIdx3 = me.getInterestIdx3();
                session.setAttribute("loginMember", ms);
            }

            ra.addFlashAttribute("updated", true);
            return "redirect:/member/mypage";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/mypage";
        }
    }

    // 변호사 프로필 수정 → LawyerService
    @PostMapping("/api/updateProfileLawyer")
    @Transactional
    public String updateProfileLawyer(@ModelAttribute LawyerDTO dto,
                                      @RequestParam(required = false) String newPassword,
                                      @RequestParam(required = false) String confirmPassword,
                                      @SessionAttribute(value = "loginUser", required = false) SessionUser loginUser,
                                      HttpSession session,
                                      RedirectAttributes ra) {
        if (loginUser == null || !"LAWYER".equalsIgnoreCase(loginUser.role)) {
            return "redirect:/member/login";
        }
        if (loginUser.lawyerIdx == null ||
           (dto.getLawyerIdx() != null && !loginUser.lawyerIdx.equals(dto.getLawyerIdx()))) {
            return "redirect:/member/login";
        }

        try {
            var result = lawyerService.updateProfileFromPortal(dto, newPassword, confirmPassword,
                                                               loginUser.userIdx, loginUser.lawyerIdx);

            // 세션 갱신
            loginUser.userId = result.newUserId();
            session.setAttribute("loginUser", loginUser);

            Object lsObj = session.getAttribute("loginLawyer");
            if (lsObj instanceof LawyerSession ls) {
                LawyerEntity le = result.lawyer();
                ls.lawyerId = le.getLawyerId();
                ls.lawyerEmail = le.getLawyerEmail();
                ls.interestIdx = le.getInterestIdx();
                session.setAttribute("loginLawyer", ls);
            }

            ra.addFlashAttribute("updated", true);
            return "redirect:/member/mypage";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/mypage";
        }
    }

    // 아이디 찾기 / 비밀번호 재설정 → MemberService
    @PostMapping("/api/findId")
    @ResponseBody
    public String findId(@RequestParam String memberPhone,
                         @RequestParam String memberIdnum) {
        return memberService.findId(memberPhone, memberIdnum);
    }

    @PostMapping("/api/resetPw")
    @ResponseBody
    @Transactional
    public String resetPw(@RequestParam String memberId,
                          @RequestParam String memberPhone,
                          @RequestParam String memberIdnum,
                          @RequestParam String newPassword,
                          @RequestParam String confirmPassword) {
        return memberService.resetPassword(memberId, memberPhone, memberIdnum, newPassword, confirmPassword);
    }

    // 내부 유틸/세션 DTO
    private static String roleUpper(String s){ return s == null ? "" : s.toUpperCase(); }
    private static String safe(String s) { return s == null ? "" : s; }

    public static class SessionUser {
        public Long userIdx;
        public String userId;
        public String role; // MEMBER/LAWYER/ADMIN
        public Integer memberIdx;
        public Integer lawyerIdx;
        public Integer adminIdx;
        public SessionUser(Long userIdx, String userId, String role,
                           Integer memberIdx, Integer lawyerIdx, Integer adminIdx) {
            this.userIdx = userIdx; this.userId = userId; this.role = role;
            this.memberIdx = memberIdx; this.lawyerIdx = lawyerIdx; this.adminIdx = adminIdx;
        }
    }
    public static class MemberSession {
        public Integer memberIdx;
        public String memberId;
        public String memberName;
        public String memberEmail;
        public String memberPhone;
        public String memberNickname;
        public Integer interestIdx1, interestIdx2, interestIdx3;
        public MemberSession(Integer memberIdx, String memberId, String memberName, String memberEmail,
                             String memberPhone, String memberNickname,
                             Integer interestIdx1, Integer interestIdx2, Integer interestIdx3) {
            this.memberIdx = memberIdx; this.memberId = memberId; this.memberName = memberName;
            this.memberEmail = memberEmail; this.memberPhone = memberPhone; this.memberNickname = memberNickname;
            this.interestIdx1 = interestIdx1; this.interestIdx2 = interestIdx2; this.interestIdx3 = interestIdx3;
        }
    }
    @Data
    public static class LawyerSession {
        public Integer lawyerIdx;
        public String lawyerId;
        public String lawyerName;
        public String lawyerEmail;
        public String lawyerPhone;
        public Integer interestIdx;
        public LawyerSession(Integer lawyerIdx, String lawyerId, String lawyerName, String lawyerEmail,
                             String lawyerPhone, Integer interestIdx) {
            this.lawyerIdx = lawyerIdx; this.lawyerId = lawyerId; this.lawyerName = lawyerName;
            this.lawyerEmail = lawyerEmail; this.lawyerPhone = lawyerPhone; this.interestIdx = interestIdx;
        }
        public Object getLawyerIdx() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getLawyerIdx'");
        }
    }
    @Data
public static class AdminSession {

    private Integer adminIdx;
    private String adminId;
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String adminRole;   // ← 꼭 넣어두기

    public AdminSession(Integer adminIdx,
                        String adminId,
                        String adminName,
                        String adminEmail,
                        String adminPhone,
                        String adminRole) {
        this.adminIdx = adminIdx;
        this.adminId = adminId;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPhone = adminPhone;
        this.adminRole = adminRole;
    }
}

    @GetMapping("/oauth2/additional-info")
    public String showAdditionalInfoForm() {
        return "member/oauthJoin"; // 추가 정보 입력 폼 HTML
    }
    @PostMapping("/oauth2/complete")
    public void saveAdditionalInfo(@ModelAttribute("member123")MemberDTO memberDTO,
                                    HttpSession session, HttpServletResponse response) throws IOException{

        TemporaryOauthDTO tempUser = (TemporaryOauthDTO) session.getAttribute("oauth2TempUser");
        if (tempUser == null) {
            response.sendRedirect("/member/login");
            return;
        }
        MemberEntity savedUser = memberService.saveProcess(memberDTO, tempUser);
        session.removeAttribute("oauth2TempUser");
        String token = jwtProvider.createToken(savedUser);
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", HttpServletResponse.SC_OK);
        responseMap.put("message", "Login successful");

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("email", savedUser.getMemberEmail());
        dataMap.put("name", savedUser.getMemberName());
        dataMap.put("token", token);

        responseMap.put("data", dataMap);

        // JSON으로 직렬화 후 응답
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(responseMap);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}