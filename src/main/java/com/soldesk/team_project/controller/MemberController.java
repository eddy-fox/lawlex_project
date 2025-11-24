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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.PointDTO;
import com.soldesk.team_project.dto.ProductDTO;
import com.soldesk.team_project.dto.PurchaseDTO;
import com.soldesk.team_project.dto.ReboardDTO;
import com.soldesk.team_project.dto.TemporaryOauthDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
// ↓ 내가 쓴 글 / 댓글용 DTO
import com.soldesk.team_project.dto.BoardDTO;
import com.soldesk.team_project.dto.CommentDTO;

import com.soldesk.team_project.repository.AdminRepository;
import com.soldesk.team_project.repository.BoardRepository;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.ReBoardRepository;
import com.soldesk.team_project.security.JwtProvider;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.PurchaseService;
import com.soldesk.team_project.service.PythonService;
import com.soldesk.team_project.service.RankingService;

import jakarta.servlet.http.Cookie;
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
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.InterestEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.ReBoardEntity;
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
    private final RankingService rankingService;
    private final com.soldesk.team_project.service.CalendarService calendarService;

    private final MemberRepository memberRepository;
    private final LawyerRepository lawyerRepository;
    private final AdminRepository adminRepository;
    private final InterestRepository interestRepository; // 로이어 관심 1개를 조인으로 세팅할 때 사용
    private final BoardRepository boardRepository;
    private final ReBoardRepository reBoardRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;

    // /member 또는 /member/ 요청 시 메인 (HomeController가 처리)
    // HomeController에 /member 매핑 추가됨

    // -------------------- 포인트 --------------------
    @GetMapping("/point")
    public String pointMain(Model model, RedirectAttributes redirectAttributes,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        // 세션에서 회원 가져오기
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("alert", "세션이 만료되었습니다.");
            return "redirect:/member/login";
        } else if (loginUser.getMemberIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "올바르지 않은 접근입니다.");
            return "redirect:/";
        } 

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
    @PostMapping("/point/prepare")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> preparePurchase(
            @RequestBody Map<String, Object> request,
            @SessionAttribute("loginUser") UserMasterDTO loginUser) {
        
        if (loginUser == null || loginUser.getMemberIdx() == null) {
            return ResponseEntity.status(401).body(Map.of("error", "인증 필요"));
        }

        try {
            int productIdx = (Integer) request.get("productIdx");
            String orderId = (String) request.get("orderId");
            int memberIdx = (Integer) request.get("memberIdx");

            // 주문 정보 생성
            PurchaseDTO purchase = purchaseService.createPendingPurchase(
                productIdx, orderId, memberIdx);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "orderId", orderId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/point")
    public String productPurchase(@RequestParam("selectedProduct") int productNum, Model model, RedirectAttributes redirectAttributes,
                                  @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
                                    
        // 세션에서 회원 가져오기
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("alert", "세션이 만료되었습니다.");
            return "redirect:/member/login";
        } else if (loginUser.getMemberIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "올바르지 않은 접근입니다.");
            return "redirect:/";
        } 

        Integer memberIdx = loginUser.getMemberIdx();
        MemberDTO member = memberService.searchSessionMember(memberIdx);
        model.addAttribute("member", member);

        ProductDTO product = purchaseService.getProduct(productNum);
        model.addAttribute("product", product);

        String purchaseId = "order-" + System.currentTimeMillis();
        PurchaseDTO purchase = purchaseService.createPendingPurchase(productNum, purchaseId, memberIdx);
        model.addAttribute("purchase", purchase);

        return "payment/checkout";
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

        // 🔹 탈퇴(비활성) 회원이면 로그인 차단
        if (m.getMemberActive() != null && m.getMemberActive() == 0) {
            return "redirect:/member/login?error=deactivated";
        }

        if (!passwordMatches(rawPw, m.getMemberPass())) {
            return "redirect:/member/login?error=badpw";
        }

        UserMasterDTO um = new UserMasterDTO();
        um.setUserId(m.getMemberId());
        um.setRole("MEMBER");
        um.setMemberIdx(m.getMemberIdx());
        um.setLawyerIdx(null);
        um.setAdminIdx(null);
        session.setAttribute("loginUser", um);
        System.out.println("✅ 세션 저장 성공! 로그인 사용자 ID: " + um.getUserId());

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

        // 🔹 탈퇴(비활성) 변호사이면 로그인 차단
        if (l.getLawyerActive() != null && l.getLawyerActive() == 0) {
            return "redirect:/member/login?error=deactivated";
        }

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
            if (dto.getMemberAgree() == null || !"1".equals(dto.getMemberAgree())) {
                return ResponseEntity.badRequest().body("개인정보 수신동의가 필요합니다.");
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
    public String logout(HttpSession session, HttpServletResponse response) {
        // 세션 무효화
        session.invalidate();
        
        // JWT 토큰 쿠키 삭제
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // 즉시 삭제
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);
        
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
    public String gmodify(@SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                          @RequestParam(value = "memberIdx", required = false) Integer memberIdxParam,
                          HttpSession session,
                          Model model) {
        if (loginUser == null) return "redirect:/member/login";
        
        // 관리자가 다른 회원 정보를 수정하는 경우
        if (loginUser.getAdminIdx() != null && memberIdxParam != null) {
            AdminEntity loginAdmin = getLoginAdmin(session);
            if (loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole())) {
                MemberDTO member = memberService.getMemberByIdx(memberIdxParam);
                if (member == null) {
                    return "redirect:/admin/memberManagement";
                }
                model.addAttribute("member", member);
                model.addAttribute("isAdminEdit", true);
                model.addAttribute("memberIdx", memberIdxParam);
                return "member/gmodify";
            }
        }
        
        // 일반회원이 자신의 정보를 수정하는 경우
        if (!"MEMBER".equalsIgnoreCase(loginUser.getRole()) || loginUser.getMemberIdx() == null) {
            return "redirect:/member/login";
        }
        MemberDTO member = memberService.getSessionMember();
        model.addAttribute("member", member);
        model.addAttribute("isAdminEdit", false);
        return "member/gmodify";
    }

    @GetMapping("/lmodify")
    public String lmodify(@SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                          @RequestParam(value = "lawyerIdx", required = false) Integer lawyerIdxParam,
                          HttpSession session,
                          Model model) {

        AdminEntity loginAdmin = getLoginAdmin(session);
        boolean adminSession = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        boolean adminUser = loginUser != null && loginUser.getAdminIdx() != null;
        boolean isAdmin = adminSession || adminUser;

        if (loginUser == null && !adminSession) {
            return "redirect:/member/login";
        }
        
        // 관리자가 다른 변호사 정보를 수정하는 경우
        if (isAdmin && lawyerIdxParam != null) {
            LawyerDTO lawyer = lawyerService.getLawyerByIdx(lawyerIdxParam);
            if (lawyer == null) {
                return "redirect:/admin/lawyerManagement";
            }
            model.addAttribute("lawyer", lawyer);
            model.addAttribute("interests", interestRepository.findAllByOrderByInterestNameAsc());
            
            // 기존 상담 가능 시간 불러오기
            var calendarList = calendarService.findAllActiveByLawyer(lawyerIdxParam);
            model.addAttribute("calendarList", calendarList);
            model.addAttribute("isAdminEdit", true);
            model.addAttribute("lawyerIdx", lawyerIdxParam);
            return "member/lmodify";
        }

        if (isAdmin && lawyerIdxParam == null) {
            return "redirect:/admin/lawyerManagement";
        }

        if (loginUser == null) {
            return "redirect:/member/login";
        }
        
        // 변호사가 자신의 정보를 수정하는 경우
        if (!"LAWYER".equalsIgnoreCase(loginUser.getRole()) || loginUser.getLawyerIdx() == null) {
            return "redirect:/member/login";
        }
        
        LawyerDTO lawyer = lawyerService.getSessionLawyer();
        model.addAttribute("lawyer", lawyer);
        model.addAttribute("interests", interestRepository.findAllByOrderByInterestNameAsc());
        
        // 기존 상담 가능 시간 불러오기
        var calendarList = calendarService.findAllActiveByLawyer(loginUser.getLawyerIdx());
        model.addAttribute("calendarList", calendarList);
        model.addAttribute("isAdminEdit", false);
        
        return "member/lmodify";
    }

    @GetMapping("/mypage")
    public String mypage(@SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                     @RequestParam(value = "memberIdx", required = false) Integer memberIdxParam,
                     @RequestParam(value = "lawyerIdx", required = false) Integer lawyerIdxParam,
                     Model model, RedirectAttributes redirectAttributes,
                     HttpSession session) {
        
        // 강제로 출력 버퍼 플러시
        System.out.flush();
        System.err.flush();
        
        System.out.println("========================================");
        System.out.println("[DEBUG] MemberController.mypage - 메서드 진입!!!");
        System.out.println("[DEBUG] MemberController.mypage - loginUser: " + (loginUser != null ? "not null" : "null"));
        System.out.println("[DEBUG] MemberController.mypage - memberIdxParam: " + memberIdxParam);
        System.out.println("[DEBUG] MemberController.mypage - lawyerIdxParam: " + lawyerIdxParam);
        System.out.println("========================================");
        
        System.out.flush();
        System.err.flush();
                        
        if (loginUser == null) {
            AdminEntity loginAdmin = getLoginAdmin(session);
            if (loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole())) {
                loginUser = UserMasterDTO.builder()
                        .adminIdx(loginAdmin.getAdminIdx())
                        .role("ADMIN")
                        .userId(loginAdmin.getAdminId())
                        .build();
            } else {
                System.out.println("[DEBUG] MemberController.mypage - loginUser가 null, 로그인 페이지로 리다이렉트");
                return "redirect:/member/login";
            }
        }
        model.addAttribute("loginUser", loginUser);

    // 관리자가 다른 회원/변호사의 마이페이지를 볼 경우
    if (loginUser.getAdminIdx() != null && (memberIdxParam != null || lawyerIdxParam != null)) {
        // 관리자가 일반 회원의 마이페이지 조회
        if (memberIdxParam != null) {
            MemberDTO member = memberService.getMemberByIdx(memberIdxParam);
            if (member == null) {
                return "redirect:/admin/memberManagement";
            }
            model.addAttribute("member", member);
            model.addAttribute("isAdminView", true);
            model.addAttribute("memberIdx", memberIdxParam);
            
            List<BoardDTO> myBoards = memberService.getMyBoards(memberIdxParam);
            List<CommentDTO> myComments = memberService.getMyComments(memberIdxParam);
            model.addAttribute("myBoards", myBoards);
            model.addAttribute("myComments", myComments);
            
            return "member/ginfo";
        } else if (lawyerIdxParam != null) {
            // 관리자가 변호사의 마이페이지 조회
            LawyerDTO lawyer = lawyerService.getLawyerByIdx(lawyerIdxParam);
            if (lawyer == null) {
                return "redirect:/admin/lawyerManagement";
            }
            model.addAttribute("lawyer", lawyer);
            model.addAttribute("isAdminView", true);
            model.addAttribute("lawyerIdx", lawyerIdxParam);
            
            List<ReboardDTO> myReboards = lawyerService.getMyReboardsForLawyer(lawyerIdxParam);
            model.addAttribute("myReboards", myReboards);
            
            // 변호사가 쓴 댓글 5개 (lawyerIdx 기준)
            List<CommentDTO> myComments = memberService.getMyCommentsByLawyer(lawyerIdxParam);
            model.addAttribute("myComments", myComments);
            
            int likeRanking = rankingService.getLikeRanking(lawyerIdxParam);
            int answerRanking = rankingService.getAnswerRanking(lawyerIdxParam);
            model.addAttribute("likeRanking", likeRanking);
            model.addAttribute("answerRanking", answerRanking);
            
            // 상담 가능 요일 및 시간대
            var calendarList = calendarService.findAllActiveByLawyer(lawyerIdxParam);
            model.addAttribute("calendarList", calendarList);
            
            return "member/linfo";
        }
    }

    // 일반 사용자가 자신의 마이페이지 조회
    String role = loginUser.getRole() == null ? "" : loginUser.getRole().toUpperCase();
    System.out.println("[DEBUG] MemberController.mypage - role: " + role);

    if ("MEMBER".equals(role)) {
        System.out.println("[DEBUG] MemberController.mypage - MEMBER 경로 진입");
        Integer memberIdx = loginUser.getMemberIdx();
        System.out.println("[DEBUG] MemberController.mypage - memberIdx: " + memberIdx);
        
        try {
            // 프로필
            System.out.println("[DEBUG] MemberController.mypage - getSessionMember() 호출 전");
            MemberDTO me = memberService.getSessionMember();
            System.out.println("[DEBUG] MemberController.mypage - getSessionMember() 완료, me: " + (me != null ? "not null" : "null"));
            model.addAttribute("member", me);

            // 내가 쓴 글 / 댓글 (memberIdx 기준)
            System.out.println("[DEBUG] MemberController.mypage - getMyBoards() 호출 전");
            List<BoardDTO> myBoards = memberService.getMyBoards(memberIdx);
            System.out.println("[DEBUG] MemberController.mypage - getMyBoards() 완료, size: " + (myBoards != null ? myBoards.size() : "null"));
            
            System.out.println("[DEBUG] MemberController.mypage - getMyComments() 호출 전");
            List<CommentDTO> myComments = memberService.getMyComments(memberIdx);
            System.out.println("[DEBUG] MemberController.mypage - getMyComments() 완료, size: " + (myComments != null ? myComments.size() : "null"));

            model.addAttribute("myBoards", myBoards);
            model.addAttribute("myComments", myComments);

            System.out.println("[DEBUG] MemberController.mypage - member/ginfo 반환 전");
            return "member/ginfo";
        } catch (Exception e) {
            System.err.println("[ERROR] MemberController.mypage - 오류 발생: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("alert", "마이페이지 조회 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }

    } else if ("LAWYER".equals(role)) {

        // 🔹 세션 기반 변호사 프로필
        LawyerDTO me = lawyerService.getSessionLawyer();
        model.addAttribute("lawyer", me);

        // 🔹 변호사가 쓴 리보드 5개 (lawyerIdx 기준)
        Integer lawyerIdx = loginUser.getLawyerIdx();
        List<ReboardDTO> myReboards = lawyerService.getMyReboardsForLawyer(lawyerIdx);
        model.addAttribute("myReboards", myReboards);

        // 🔹 변호사가 쓴 댓글 5개 (lawyerIdx 기준)
        List<CommentDTO> myComments = memberService.getMyCommentsByLawyer(lawyerIdx);
        model.addAttribute("myComments", myComments);

        // 🔹 랭킹 계산 (변호사 프로필 페이지와 동일한 방식)
        int likeRanking = rankingService.getLikeRanking(lawyerIdx);
        int answerRanking = rankingService.getAnswerRanking(lawyerIdx);
        model.addAttribute("likeRanking", likeRanking);
        model.addAttribute("answerRanking", answerRanking);

        // 🔹 상담 가능 요일 및 시간대
        var calendarList = calendarService.findAllActiveByLawyer(lawyerIdx);
        model.addAttribute("calendarList", calendarList);

        return "member/linfo";

    } else if ("ADMIN".equals(role)) {
        // 관리자는 관리자 메인 페이지로 리다이렉트
        return "redirect:/admin/memberManagement";

    } else {
        return "redirect:/member/login";
    }
}

    // 내가 쓴 글 리스트 (일반회원: 상담글, 변호사: 답변글)
    @GetMapping("/mypage/myPosts")
    public String myPosts(@SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          Model model) {

        if (loginUser == null) return "redirect:/member/login";

        if (page < 0) page = 0;

        org.springframework.data.domain.PageRequest pageable =
            org.springframework.data.domain.PageRequest.of(page, 10);

        String role = loginUser.getRole() == null ? "" : loginUser.getRole().toUpperCase();

        if ("MEMBER".equals(role) && loginUser.getMemberIdx() != null) {
            // 일반회원: 내가 쓴 글 리스트 (board.member.memberIdx == loginUser.memberIdx)
            org.springframework.data.domain.Page<BoardEntity> paging =
                boardRepository.findByMemberMemberIdxOrderByBoardRegDateDesc(
                    loginUser.getMemberIdx(), pageable);

            int currentBlock = page / 10;
            int startPage = currentBlock * 10;
            int totalPages = paging.getTotalPages();
            int endPage = Math.min(startPage + 9, (totalPages > 0 ? totalPages - 1 : 0));

            model.addAttribute("paging", paging);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
            model.addAttribute("userType", "MEMBER");
            return "member/myPosts";

        } else if ("LAWYER".equals(role) && loginUser.getLawyerIdx() != null) {
            // 변호사: 답변글 리스트
            org.springframework.data.domain.Page<ReBoardEntity> paging =
                reBoardRepository.findByLawyerLawyerIdxOrderByReboardRegDateDesc(
                    loginUser.getLawyerIdx(), pageable);

            int currentBlock = page / 10;
            int startPage = currentBlock * 10;
            int totalPages = paging.getTotalPages();
            int endPage = Math.min(startPage + 9, (totalPages > 0 ? totalPages - 1 : 0));

            model.addAttribute("paging", paging);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
            model.addAttribute("userType", "LAWYER");
            return "member/myPosts";
        }

        return "redirect:/member/mypage";
    }

    // 내가 댓글을 남긴 newsboard 게시글 리스트
    @GetMapping("/mypage/myComments")
    public String myComments(@SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             Model model) {
        if (loginUser == null) return "redirect:/member/login";

        org.springframework.data.domain.PageRequest pageable = 
            org.springframework.data.domain.PageRequest.of(page, 10);

        if ("MEMBER".equalsIgnoreCase(loginUser.getRole()) && loginUser.getMemberIdx() != null) {
            org.springframework.data.domain.Page<com.soldesk.team_project.entity.NewsBoardEntity> paging = 
                memberService.getMyCommentedNewsBoards(loginUser.getMemberIdx(), pageable);
            
            // 페이징 범위 계산
            int currentBlock = page / 10;
            int startPage = currentBlock * 10;
            int endPage = Math.min(startPage + 9, paging.getTotalPages() - 1);
            
            model.addAttribute("paging", paging);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
            model.addAttribute("userType", "MEMBER");
            model.addAttribute("pageTitle", "내가 쓴 댓글");
            return "member/myComments";
        } else if ("LAWYER".equalsIgnoreCase(loginUser.getRole()) && loginUser.getLawyerIdx() != null) {
            org.springframework.data.domain.Page<com.soldesk.team_project.entity.NewsBoardEntity> paging = 
                memberService.getMyCommentedNewsBoardsByLawyer(loginUser.getLawyerIdx(), pageable);
            
            // 페이징 범위 계산
            int currentBlock = page / 10;
            int startPage = currentBlock * 10;
            int endPage = Math.min(startPage + 9, paging.getTotalPages() - 1);
            
            model.addAttribute("paging", paging);
            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
            model.addAttribute("userType", "LAWYER");
            model.addAttribute("pageTitle", "내가 쓴 댓글");
            return "member/myComments";
        }
        
        return "redirect:/member/mypage";
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
    

    // OAuth로 회원가입
    @GetMapping("/joinType-oauth")
    public String OAuth2JoinChoice() {
        return "member/loginChoice-oauth";
    }

    // OAuth 일반회원
    @GetMapping("/joinMember-oauth")
    public String OAuth2JoinMemberForm(HttpSession session, Model model, 
        RedirectAttributes redirectAttributes) {

        TemporaryOauthDTO temp = (TemporaryOauthDTO) session.getAttribute("tempOauth");
    
        if (temp == null) {
            redirectAttributes.addFlashAttribute("alert", "올바르지 않은 접근입니다.");
            return "redirect:/member/login";
        }
    
        MemberDTO joinMember = new MemberDTO();
        joinMember.setMemberEmail(temp.getEmail());
        joinMember.setMemberName(temp.getName());
        
        model.addAttribute("joinMember", joinMember);
        model.addAttribute("interests", interestRepository.findAll());

        return "member/gJoin-oauth";
    }
    @PostMapping("/joinMember-oauth")
    public String OAuth2JoinMemberSubmit(HttpSession session,
        @ModelAttribute("joinMember") MemberDTO joinMember) {

        TemporaryOauthDTO temp = (TemporaryOauthDTO) session.getAttribute("tempOauth");

        memberService.joinOAuthMember(temp, joinMember);

        session.removeAttribute("tempOauth");
            
        return "redirect:/member/login";
    }

    // OAuth 변호사회원
    @GetMapping("/joinLawyer-oauth")
    public String OAuth2JoinLawyerForm(HttpSession session, Model model, 
        RedirectAttributes redirectAttributes) {

        TemporaryOauthDTO temp = (TemporaryOauthDTO) session.getAttribute("tempOauth");
    
        if (temp == null) {
            redirectAttributes.addFlashAttribute("alert", "올바르지 않은 접근입니다.");
            return "redirect:/member/login";
        }
    
        LawyerDTO joinLawyer = new LawyerDTO();
        joinLawyer.setLawyerEmail(temp.getEmail());
        joinLawyer.setLawyerName(temp.getName());
        
        model.addAttribute("joinLawyer", joinLawyer);
        model.addAttribute("interests", interestRepository.findAll());

        return "member/lJoin-oauth";
    }
    @PostMapping("/joinLawyer-oauth")
    public String OAuth2JoinLawyerSubmit(HttpSession session,
        @ModelAttribute("joinLawyer") LawyerDTO joinLawyer,
        @RequestParam(value = "lawyerImage", required = false) MultipartFile lawyerImage,
        @RequestParam(value = "availabilityJson", required = false) String availabilityJson) {

        TemporaryOauthDTO temp = (TemporaryOauthDTO) session.getAttribute("tempOauth");

        System.out.println("🔍 받은 interestIdx: " + joinLawyer.getLawyerAuth());
    
        lawyerService.joinOAuthLawyer(temp, joinLawyer, lawyerImage, availabilityJson);

        session.removeAttribute("tempOauth");
            
        return "redirect:/member/login";
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



    // 현재 회원 프로필
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
            @RequestParam(value="confirmPassword", required=false) String confirmPassword,
            @RequestParam(value="memberIdx", required=false) Integer memberIdxParam,
            HttpSession session) {

        if (loginUser == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        
        try {
            // 관리자가 다른 회원 정보를 수정하는 경우
            if (loginUser.getAdminIdx() != null && memberIdxParam != null) {
                AdminEntity loginAdmin = getLoginAdmin(session);
                if (loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole())) {
                    // 관리자 권한으로 다른 회원 정보 수정
                    memberService.updateProfileForMemberByIdx(memberIdxParam, form, newPassword, confirmPassword);
                    return ResponseEntity.ok("OK");
                }
            }
            
            // 일반회원이 자신의 정보를 수정하는 경우
            if (!"MEMBER".equalsIgnoreCase(loginUser.getRole())) {
                return ResponseEntity.status(401).body("UNAUTHORIZED");
            }
            
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

    // =================== 변호사 상담시간 설정 API ===================
    /**
     * 변호사 마이페이지 > 상담시간 설정 저장
     * - 요청 Body(JSON) 예시:
     *   [
     *     { "weekdays": [0, 2, 4], "start": "09:00", "end": "12:00" },
     *     { "weekdays": [1],       "start": "13:00", "end": "18:00" }
     *   ]
     * - CalendarService.updateAvailabilityMultiple() 사용해서
     *   해당 변호사의 calendar_active를 0/1로 갱신
     */
    @PostMapping(value = "/api/lawyer/calendar", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateLawyerCalendar(
            @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
            @RequestBody List<Map<String, Object>> timeSlots,
            @RequestParam(value = "lawyerIdx", required = false) Integer lawyerIdxParam,
            HttpSession session
    ) {
        Map<String, Object> res = new HashMap<>();

        try {
            AdminEntity loginAdmin = getLoginAdmin(session);
            boolean isAdmin = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());

            Integer lawyerIdx;
            if (isAdmin && lawyerIdxParam != null) {
                lawyerIdx = lawyerIdxParam;
            } else {
                if (loginUser == null || !"LAWYER".equalsIgnoreCase(loginUser.getRole())) {
                    res.put("success", false);
                    res.put("message", "로그인이 필요하거나 변호사 계정이 아닙니다.");
                    return ResponseEntity.status(401).body(res);
                }
                lawyerIdx = loginUser.getLawyerIdx();
            }

            calendarService.updateAvailabilityMultiple(lawyerIdx, timeSlots);

            res.put("success", true);
            res.put("message", "상담 가능 시간이 저장되었습니다.");
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(res);
        }
    }

    private AdminEntity getLoginAdmin(HttpSession session) {
        Object obj = session.getAttribute("loginAdmin");
        if (obj == null) return null;

        if (obj instanceof AdminEntity ae) {
            return ae;
        }
        if (obj instanceof AdminSession as) {
            return adminRepository.findById(as.getAdminIdx()).orElse(null);
        }
        return null;
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
