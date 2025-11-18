package com.soldesk.team_project.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.PointDTO;
import com.soldesk.team_project.dto.ProductDTO;
import com.soldesk.team_project.dto.PurchaseDTO;
import com.soldesk.team_project.dto.TemporaryOauthDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.repository.AdminRepository;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.security.JwtProvider;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.PurchaseService;
import com.soldesk.team_project.service.PythonService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import com.soldesk.team_project.repository.*;
import com.soldesk.team_project.security.JwtProvider;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.soldesk.team_project.entity.AdminEntity;
import com.soldesk.team_project.entity.InterestEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
// ===== [ADD] gmodify에 interests 바인딩용 =====
import com.soldesk.team_project.entity.InterestEntity;

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

    // /member 또는 /member/ 요청 시 메인 (HomeController가 처리)
    // HomeController에 /member 매핑 추가됨

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
                            Model model) {
        if (need != null) model.addAttribute("need", need);
        return "member/login";
    }

    /**
     * 유저마스터 테이블 없이 로그인:
     * 1) member -> 2) lawyer -> 3) admin 순으로 ID 조회
     * - 없으면 /member/login?error=nouser
     * - 있으면 비번 하이브리드(BCrypt/평문) 매칭
     *   * 틀리면 /member/login?error=badpw
     *   * 맞으면 UserMasterDTO 구성하여 세션(loginUser)에 저장 + 각 역할 세션도 저장
     *   * need=mypage 로 들어오면 로그인 직후 /member/mypage 로 이동
     */
    @PostMapping("/login")
    public String loginSubmit(@RequestParam("memberId") String userId,
                              @RequestParam("memberPass") String rawPw,
                              @RequestParam(name = "need", required = false) String need,
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
            return "mypage".equalsIgnoreCase(need) ? "redirect:/member/mypage" : "redirect:/member";
        }

        // 2) LAWYER
        var lOpt = lawyerRepository.findByLawyerId(userId);
        if (lOpt.isPresent()) {
            LawyerEntity l = lOpt.get();

            // 비밀번호 확인
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
            return "mypage".equalsIgnoreCase(need) ? "redirect:/member/mypage" : "redirect:/member";
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
            return "mypage".equalsIgnoreCase(need) ? "redirect:/member/mypage" : "redirect:/member";
        }

        // 전부 없음
        return "redirect:/member/login?error=nouser";
    }

    // 일반회원가입 처리
    @PostMapping(value = "/join/normal", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> joinNormalSubmit(@ModelAttribute MemberDTO dto) {
        try {
            // 필수 클라이언트 검증이 있어도 서버에서 한 번 더 안전장치
            if (dto.getMemberAgree() == null || !"Y".equalsIgnoreCase(dto.getMemberAgree())) {
                return ResponseEntity.badRequest().body("개인정보 수신동의(Y)가 필요합니다.");
            }
            // 관심 분야 3개 모두 선택 + 서로 달라야 함
            Integer i1 = dto.getInterestIdx1(), i2 = dto.getInterestIdx2(), i3 = dto.getInterestIdx3();
            if (i1 == null || i2 == null || i3 == null) {
                return ResponseEntity.badRequest().body("관심 분야 3개를 모두 선택해주세요.");
            }
            if (i1.equals(i2) || i1.equals(i3) || i2.equals(i3)) {
                return ResponseEntity.badRequest().body("관심 분야는 서로 다른 항목으로 선택해주세요.");
            }

            // 실제 가입 처리
            memberService.joinNormal(dto);

            // fetch로 받는 쪽에서 redirected 처리할 수 있게 302로 로그인으로 보냄
            return ResponseEntity.status(302)
                    .header("Location", "/member/login?joined=true")
                    .body("OK");
        } catch (IllegalArgumentException e) {
            // 서비스에서 던진 구체 메시지 그대로 내려줌
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }
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
    public String joinNormalForm(Model model){
        model.addAttribute("interests", interestRepository.findAll());
        return "member/gjoin";
    }

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

    // 컨트롤 아이디중복확인 멤버
    @GetMapping(value = "/api/checkId", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String checkId(@RequestParam("memberId") String memberId) {
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

    // =================== 세션 DTO들 ===================

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

    // 비밀번호 암호화 + 평문(예: 1234) 허용
    private boolean passwordMatches(String raw, String db) {
        if (db == null) return false;
        db = db.trim();
        if (db.startsWith("{bcrypt}")) { // 실수로 접두 저장된 경우 대비
            db = db.substring("{bcrypt}".length());
        }
        boolean isBcrypt = db.startsWith("$2a$") || db.startsWith("$2b$") || db.startsWith("$2y$");
        return isBcrypt ? passwordEncoder.matches(raw, db) : raw.equals(db);
    }

    /* ===================== [ADD] gmodify용 공통 모델 & 프로필/비번/탈퇴 API ===================== */

    // gmodify 템플릿에서 사용할 현재 회원 프로필
    @ModelAttribute("m")
    public MemberDTO exposeMemberForModify(
            @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        try {
            if (loginUser != null && "MEMBER".equalsIgnoreCase(loginUser.getRole())) {
                return memberService.loadProfileForModify();
            }
        } catch (Exception ignore) {}
        return null;
    }

    // gmodify에서 노출할 관심분야 리스트
    @ModelAttribute("interests")
    public List<InterestEntity> exposeInterests() {
        return interestRepository.findAll();
    }

    // 프로필(닉네임/이메일/관심분야 3개 + 선택적 비번변경) 저장
    @PostMapping(value="/api/profile", produces="text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> updateProfileForMember(
            @SessionAttribute(value="loginUser", required = false) UserMasterDTO loginUser,
            @ModelAttribute MemberDTO form,
            @RequestParam(value="newPassword", required=false) String newPassword,
            @RequestParam(value="confirmPassword", required=false) String confirmPassword) {

        if (loginUser == null || !"MEMBER".equalsIgnoreCase(loginUser.getRole())) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        try {
            var result = memberService.updateProfileForCurrent(form, newPassword, confirmPassword);
            return ResponseEntity.ok("OK");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("SERVER_ERROR");
        }
    }

    // 비밀번호 변경(전화번호+생년월일 검증)
    @PostMapping(value="/api/changePassword", produces="text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> changePasswordWithVerification(
            @SessionAttribute(value="loginUser", required = false) UserMasterDTO loginUser,
            @RequestParam("memberPhone") String memberPhone,
            @RequestParam("memberIdnum")  String memberIdnum,
            @RequestParam("newPassword")  String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {

        if (loginUser == null || !"MEMBER".equalsIgnoreCase(loginUser.getRole())) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String res = memberService.changePasswordWithVerificationForCurrent(memberPhone, memberIdnum, newPassword, confirmPassword);
        return switch (res) {
            case "OK"       -> ResponseEntity.ok("OK");
            case "MISMATCH" -> ResponseEntity.badRequest().body("비밀번호 확인이 일치하지 않습니다.");
            default         -> ResponseEntity.badRequest().body("본인 확인에 실패했습니다.");
        };
    }

    // 회원 탈퇴(전화번호+생년월일 검증)
    @PostMapping(value="/api/deactivate", produces="text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> deactivateMember(
            @SessionAttribute(value="loginUser", required = false) UserMasterDTO loginUser,
            HttpSession session,
            @RequestParam("memberPhone") String memberPhone,
            @RequestParam("memberIdnum")  String memberIdnum) {

        if (loginUser == null || !"MEMBER".equalsIgnoreCase(loginUser.getRole())) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        try {
            boolean ok = memberService.deactivateWithVerificationForCurrent(memberPhone, memberIdnum);
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

/*유저 마스터 dto에 저장된 세션 가져오는 코드

// 컨트롤러 예시
@GetMapping("/mypage")
public String mypage(Model model) {
    // 일반회원 화면일 때
    MemberDTO me = memberService.getSessionMember();
    model.addAttribute("member", me);
    return "member/ginfo";
}

// 다른 서비스 예시
public void doSomethingForCurrentLawyer() {
    LawyerDTO me = lawyerService.getSessionLawyer();
    // ... 로직 ...
}
*/
