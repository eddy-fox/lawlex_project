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
import com.soldesk.team_project.security.JwtProvider;

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

    private final MemberRepository memberRepository;
    private final LawyerRepository lawyerRepository;
    private final AdminRepository adminRepository;
    private final InterestRepository interestRepository; // 로이어 관심 1개를 조인으로 세팅할 때 사용
    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    // /member 또는 /member/ 요청 시 메인
    @GetMapping({"", "/"})
    public String memberRoot() { return "index"; }

    // -------------------- 포인트 --------------------
    @GetMapping("/point")
    public String pointMain(Model model, @SessionAttribute("loginUser") UserMasterDTO loginUser) {
        
        // 세션에서 회원 가져오기
        Integer memberIdx = loginUser.getMemberIdx();
        MemberDTO member = memberService.searchSessionMember(memberIdx);
        model.addAttribute("member", member);
                
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
    public String productPurchase(@RequestParam("selectedProduct") int productNum,
                                  Model model,
                                  @SessionAttribute("loginUser") UserMasterDTO loginUser) {
        Integer memberIdx = loginUser.getMemberIdx();
        String memberIdxStr = String.valueOf(memberIdx);
        List<MemberDTO> member = memberService.searchMembers("Idx", memberIdxStr);
        model.addAttribute("member", member);

        String purchaseId = "order-" + System.currentTimeMillis();
        PurchaseDTO purchase = purchaseService.createPendingPurchase(productNum, purchaseId, memberIdx);
        model.addAttribute("purchase", purchase);

        return "payment/checkout";
    }

    // -------------------- OCR 테스트 --------------------
    @GetMapping("/testOCR")
    public String testOCR(Model model) {
        LawyerDTO lawyerDTO = new LawyerDTO();
        lawyerDTO.setLawyerAuth(0);
        model.addAttribute("lawyerAuth", new LawyerDTO());
        return "member/testOCR";
    }

    @PostMapping("/verify-license")
    @ResponseBody
    public Map<String, Object> verifyLicense(@RequestParam("licenseNumber") String licenseNumber,
                                             @RequestParam("licenseImage") MultipartFile licenseImage) {
        Map<String, Object> result = new HashMap<>();
        try {
            File tempFile = File.createTempFile("license_", ".jpg");
            licenseImage.transferTo(tempFile);

            Map<String, Object> ocrResult = pythonService.runPythonOCR("ocr.py", tempFile.toString());
            if (!(boolean) ocrResult.getOrDefault("valid", false)) {
                result.put("valid", false);
                result.put("error", ocrResult.get("error"));
                return result;
            }

            @SuppressWarnings("unchecked")
            List<String> ocrTexts = (List<String>) ocrResult.get("texts");
            boolean matched = ocrTexts.stream().anyMatch(text -> text.contains(licenseNumber));

            result.put("valid", matched);
            result.put("message", matched ? "자격번호 일치!" : "자격번호 불일치!");
            result.put("ocrTexts", ocrTexts);
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
    public String loginForm(@RequestParam(name = "need", required = false) String need,
        Model model) {  if (need != null) {
        model.addAttribute("need", need);
    }
    return "member/login"; }

    /**
     * 유저마스터 테이블 없이 로그인:
     * 1) member -> 2) lawyer -> 3) admin 순으로 ID 조회
     * - 없으면 /member/login?error=nouser
     * - 있으면 비번 하이브리드(BCrypt/평문) 매칭
     *   * 틀리면 /member/login?error=badpw
     *   * 맞으면 UserMasterDTO 구성하여 세션(loginUser)에 저장 + 각 역할 세션도 저장
     */
    @PostMapping("/login")
    public String loginSubmit(@RequestParam("memberId") String userId,
                              @RequestParam("memberPass") String rawPw,
                              HttpSession session) {

        // 1) MEMBER
        var mOpt = memberRepository.findByMemberId(userId);
        if (mOpt.isPresent()) {
            MemberEntity m = mOpt.get();
            if (!passwordMatches(rawPw, m.getMemberPass())) {
                return "redirect:/member/login?error=badpw";
            }
            // loginUser: UserMasterDTO로 세션 저장
            UserMasterDTO um = new UserMasterDTO();
            um.setUserId(m.getMemberId());
            um.setRole("MEMBER");
            um.setMemberIdx(m.getMemberIdx());
            um.setLawyerIdx(null);
            um.setAdminIdx(null);
            session.setAttribute("loginUser", um);
            System.out.println("✅ 세션 저장 성공! 로그인 사용자 ID: " + um.getUserId());

            // loginMember: 상세 세션
            MemberSession ms = new MemberSession(
                    m.getMemberIdx(), m.getMemberId(), m.getMemberName(),
                    m.getMemberEmail(), m.getMemberPhone(), m.getMemberNickname(),
                    m.getInterestIdx1(), m.getInterestIdx2(), m.getInterestIdx3()
            );
            session.setAttribute("loginMember", ms);
            session.removeAttribute("loginLawyer");
            session.removeAttribute("loginAdmin");

            session.setMaxInactiveInterval(60 * 60);
            return "redirect:/member";
        }

        // 2) LAWYER
        var lOpt = lawyerRepository.findByLawyerId(userId);
        if (lOpt.isPresent()) {
            LawyerEntity l = lOpt.get();
            if (!passwordMatches(rawPw, l.getLawyerPass())) {
                return "redirect:/member/login?error=badpw";
            }
            UserMasterDTO um = new UserMasterDTO();
            um.setUserId(l.getLawyerId());
            um.setRole("LAWYER");
            um.setMemberIdx(null);
            um.setLawyerIdx(l.getLawyerIdx());
            um.setAdminIdx(null);
            session.setAttribute("loginUser", um);

            LawyerSession ls = new LawyerSession(
                    l.getLawyerIdx(), l.getLawyerId(), l.getLawyerName(),
                    l.getLawyerEmail(), l.getLawyerPhone(), l.getInterestIdx()
            );
            session.setAttribute("loginLawyer", ls);
            session.removeAttribute("loginMember");
            session.removeAttribute("loginAdmin");

            session.setMaxInactiveInterval(60 * 60);
            return "redirect:/member";
        }

        // 3) ADMIN
        var aOpt = adminRepository.findByAdminId(userId);
        if (aOpt.isPresent()) {
            AdminEntity a = aOpt.get();
            if (!passwordMatches(rawPw, a.getAdminPass())) {
                return "redirect:/member/login?error=badpw";
            }
            UserMasterDTO um = new UserMasterDTO();
            um.setUserId(a.getAdminId());
            um.setRole("ADMIN");
            um.setMemberIdx(null);
            um.setLawyerIdx(null);
            um.setAdminIdx(a.getAdminIdx());
            session.setAttribute("loginUser", um);

            AdminSession as = new AdminSession(
                    a.getAdminIdx(), a.getAdminId(), a.getAdminName(),
                    a.getAdminEmail(), a.getAdminPhone(), a.getAdminRole()
            );
            session.setAttribute("loginAdmin", as);
            session.removeAttribute("loginMember");
            session.removeAttribute("loginLawyer");

            session.setMaxInactiveInterval(60 * 60);
            return "redirect:/member";
        }

        // 전부 없음
        return "redirect:/member/login?error=nouser";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/member/login";
    }

    // 회원가입/마이페이지/수정
    @GetMapping("/join/type")
    public String joinType() { return "member/loginChoice"; }

    @GetMapping({"/join/normal", "/joinNormal"})
    public String joinNormalForm() { return "member/gjoin"; }

    @GetMapping({"/join/lawyer", "/lawyer/join"})
    public String joinLawyerForm() { return "member/ljoin"; }

    @GetMapping("/loginFind")
    public String loginFind() { return "member/loginFind"; }

    @GetMapping("/gmodify")
    public String gmodify(@SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        if (loginUser == null || !"MEMBER".equalsIgnoreCase(loginUser.getRole())) return "redirect:/member/login";
        return "member/gmodify";
    }

    @GetMapping("/lmodify")
    public String lmodify(@SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        if (loginUser == null || !"LAWYER".equalsIgnoreCase(loginUser.getRole())) return "redirect:/member/login";
        return "member/lmodify";
    }

    @GetMapping("/mypage")
    public String mypage(@SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                         Model model) {
        if (loginUser == null) return "redirect:/member/login";
        model.addAttribute("loginUser", loginUser);
        return switch (loginUser.getRole() == null ? "" : loginUser.getRole().toUpperCase()) {
            case "MEMBER" -> "member/ginfo";
            case "LAWYER" -> "member/linfo";
            case "ADMIN"  -> "admin/ainfo";
            default -> "redirect:/member/login";
        };
    }

    // 공통 API
    // 아이디 찾기
    @PostMapping("/api/findId")
    @ResponseBody
    public String findId(@RequestParam("memberPhone") String memberPhone,
                         @RequestParam("memberIdnum")  String memberIdnum) {
        return memberService.findId(memberPhone, memberIdnum);
    }

    // 비밀번호 재설정
    @PostMapping("/api/resetPw")
    @ResponseBody
    @Transactional
    public String resetPw(@RequestParam("memberId")        String memberId,
                          @RequestParam("memberPhone")     String memberPhone,
                          @RequestParam("memberIdnum")     String memberIdnum,
                          @RequestParam("newPassword")     String newPassword,
                          @RequestParam("confirmPassword") String confirmPassword) {
        return memberService.resetPassword(memberId, memberPhone, memberIdnum, newPassword, confirmPassword);
    }

    //컨트롤 아이디중복확인 멤버 + 변호사
    @GetMapping(value="/api/checkId", produces="text/plain;charset=UTF-8")
    @ResponseBody
    public String checkId(@RequestParam String memberId){
        return memberService.isUserIdDuplicate(memberId) ? "DUP" : "OK";
    }

    // OAuth
    @GetMapping("/oauth2/additional-info")
    public String showAdditionalInfoForm() {
        return "member/oauthJoin"; // 추가 정보 입력 폼 HTML
    }

    @PostMapping("/oauth2/complete")
    public void saveAdditionalInfo(@ModelAttribute("member123") MemberDTO memberDTO,
                                   HttpSession session, HttpServletResponse response) throws IOException {
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

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(responseMap);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

    // 세션 DTO들 

    @Data
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
    }

    @Data
    public static class AdminSession {
        public Integer adminIdx;
        public String adminId;
        public String adminName;
        public String adminEmail;
        public String adminPhone;
        public String adminRole;
        public AdminSession(Integer adminIdx, String adminId, String adminName,
                            String adminEmail, String adminPhone, String adminRole) {
            this.adminIdx = adminIdx; this.adminId = adminId; this.adminName = adminName;
            this.adminEmail = adminEmail; this.adminPhone = adminPhone; this.adminRole = adminRole;
        }
    }

    // 비밀번호 암호화 + 1234~~  가능하게
    private boolean passwordMatches(String raw, String db) {
        if (db == null) return false;
        db = db.trim();
        if (db.startsWith("{bcrypt}")) { // 실수로 접두 저장된 경우 대비
            db = db.substring("{bcrypt}".length());
        }
        boolean isBcrypt = db.startsWith("$2a$") || db.startsWith("$2b$") || db.startsWith("$2y$");
        return isBcrypt ? passwordEncoder.matches(raw, db) : raw.equals(db);
    }
}
