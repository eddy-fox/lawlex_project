package com.soldesk.team_project.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.security.JwtProvider;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.PurchaseService;
import com.soldesk.team_project.service.PythonService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("member")
@RequiredArgsConstructor
public class MemberController {

    private final PurchaseService purchaseService;
    private final PythonService pythonService;
    private final JwtProvider jwtProvider;
    
    // 임시 로그인
    @GetMapping("/loginTemp")
    public String loginTemp() {

        return "member/loginTemp";
    }
    @PostMapping("/loginTemp")
    public String loginTempVerify() {

        return "redirect:/";
    }

    // 멤버 포인트
    @GetMapping("/point")
    public String pointMain(Model model) {
        
        int memberIdx = 1; // 회원정보 받아와서 넘겨야함
        
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
    public String productPurchase(@RequestParam("selectedProduct") int productNum, Model model) {

        // 구매 검증을 위한 ID 생성
        String purchaseId = "order-" + System.currentTimeMillis();
        // 구매 요청 내역 생성
        PurchaseDTO purchase = purchaseService.createPendingPurchase(productNum, purchaseId); // 회원정보도 같이 줘야함
        model.addAttribute("purchase", purchase);
        
        return "payment/checkout"; // 넘어갈 때 회원 정보 같이 넘겨줘야함
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
     private final MemberService memberService;
    private final InterestRepository interestRepository;
    private final MemberRepository memberRepository;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage(@RequestParam(value="joined", required=false) String joined,
                            @RequestParam(value="deactivated", required=false) String deactivated,
                            Model model){
        if (joined != null) model.addAttribute("joined", true);
        if (deactivated != null) model.addAttribute("deactivated", true);
        return "member/login";
    }

    // 로그인 성공 후 이동
    @GetMapping("/main")
    public String main(){ return "member/main"; }

    // 마이페이지: 인증 사용자 기준 로딩
    @GetMapping("/mypage")
    public String mypage(Authentication authentication, Model model){
        if (authentication == null || !authentication.isAuthenticated()){
            return "redirect:/member/login";
        }
        String loginId = authentication.getName();
        MemberEntity me = memberRepository.findByMemberId(loginId).orElse(null);
        if (me == null){ return "redirect:/member/login"; }

        model.addAttribute("me", me);
        model.addAttribute("memberIdx", me.getMemberIdx());
        model.addAttribute("interests", interestRepository.findAll());
        return "member/mypage";
    }

    // 아이디/비번 찾기 팝업
    @GetMapping("/popup/find")
    public String popupFind(){ return "member/popup_find"; }

    // 로그인 필요 팝업
    @GetMapping("/popup/needLogin")
    public String popupNeedLogin(){ return "member/popup_need_login"; }

    // 정보수정 팝업: 인증 사용자 기준 로딩
    @GetMapping("/popup/edit")
    public String popupEdit(Authentication authentication, Model model){
        if (authentication == null || !authentication.isAuthenticated()){
            return "member/popup_need_login";
        }
        String loginId = authentication.getName();
        MemberEntity me = memberRepository.findByMemberId(loginId).orElse(null);
        if (me == null){ return "member/popup_need_login"; }

        model.addAttribute("me", me);
        model.addAttribute("interests", interestRepository.findAll());
        return "member/popup_edit";
    }

    // 아이디 중복 체크
    @GetMapping("/api/checkId")
    @ResponseBody
    public String checkId(@RequestParam String memberId){
        return memberRepository.existsByMemberId(memberId) ? "DUP" : "OK";
    }

    // 아이디 찾기: 전화/생년월일 정규화 후 조회
    @PostMapping("/api/findId")
    @ResponseBody
    public String findId(@RequestParam String memberPhone,
                         @RequestParam String memberIdnum){
        String phone = memberPhone.replaceAll("\\D", "");
        String idnum = memberIdnum.replaceAll("\\D", "");
        MemberEntity me = memberService.findByPhoneAndIdnum(phone, idnum);
        return (me == null) ? "NOT_FOUND" : (me.getMemberIdx() + "/" + me.getMemberId());
    }

    // 비밀번호 재설정
    @PostMapping("/api/resetPw")
    @ResponseBody
    public String resetPw(@RequestParam String memberId,
                          @RequestParam String memberPhone,
                          @RequestParam String memberIdnum,
                          @RequestParam String newPassword,
                          @RequestParam String confirmPassword){
        if (!newPassword.equals(confirmPassword)) return "MISMATCH";
        String phone = memberPhone.replaceAll("\\D", "");
        String idnum = memberIdnum.replaceAll("\\D", "");
        return memberService.resetPassword(memberId, phone, idnum, newPassword) ? "OK" : "FAIL";
    }

    // 정보수정: 본인검증 API (idx+전화+생년월일)
    @PostMapping("/api/verifyForEdit")
    @ResponseBody
    public String verifyForEdit(@RequestParam Integer memberIdx,
                                @RequestParam String memberPhone,
                                @RequestParam String memberIdnum) {
        String phone = memberPhone.replaceAll("\\D", "");
        String idnum = memberIdnum.replaceAll("\\D", "");
        return memberService.verifyIdxPhoneBirth(memberIdx, phone, idnum) ? "OK" : "FAIL";
    }

    // 회원유형 선택 페이지
    @GetMapping("/join/type")
    public String joinType(){ return "member/joinType"; }

    // 일반회원 가입 페이지
    @GetMapping({"/joinNormal", "/join/normal"})
    public String joinNormal(){ return "member/joinNormal"; }

    // 변호사 가입 페이지
    @GetMapping({"/join/lawyer", "/lawyer/join"})
    public String joinLawyer(){ return "lawyer/join"; }

    // /member/join → 유형 선택으로 리다이렉트
    @GetMapping("/join")
    public String joinShortcut(){ return "redirect:/member/join/type"; }

    // 회원정보 수정 저장 (닉네임/비번/관심분야)
    @PostMapping("/api/updateProfile")
    public String updateProfile(@ModelAttribute com.soldesk.team_project.dto.MemberDTO dto,
                                Authentication authentication,
                                RedirectAttributes ra) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/member/login";
        }
        String loginId = authentication.getName();
        MemberEntity me = memberRepository.findByMemberId(loginId).orElse(null);
        if (me == null || (dto.getMemberIdx() != null && !me.getMemberIdx().equals(dto.getMemberIdx()))) {
            return "redirect:/member/login";
        }

        memberService.updateProfile(dto);
        ra.addFlashAttribute("updated", true);
        return "redirect:/member/mypage";
    }

    // ✅ 회원 탈퇴(비활성화)
    @PostMapping("/api/deactivate")
    public String deactivate(@RequestParam Integer memberIdx,
                             @RequestParam String memberPhone,
                             @RequestParam String memberIdnum,
                             Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/member/login";
        }
        String phone = memberPhone.replaceAll("\\D", "");
        String idnum = memberIdnum.replaceAll("\\D", "");

        boolean ok = memberService.deactivate(memberIdx, phone, idnum);
        return ok ? "redirect:/member/login?deactivated" : "redirect:/member/mypage";
    }

    @GetMapping("/oauth2/additional-info")
    public String showAdditionalInfoForm() {
        return "member/oauthJoin"; // 추가 정보 입력 폼 HTML
    }
    @PostMapping("/oauth2/complete")
    public String saveAdditionalInfo(@ModelAttribute("member123")MemberDTO memberDTO,
                                    HttpSession session, HttpServletResponse response) {

        TemporaryOauthDTO tempUser = (TemporaryOauthDTO) session.getAttribute("oauth2TempUser");
        if (tempUser == null) {
            return "redirect:/member/login";
        }
        MemberEntity savedUser = memberService.saveProcess(memberDTO, tempUser);
        session.removeAttribute("oauth2TempUser");
        String token = jwtProvider.createToken(savedUser);
        Map<String, Object> loginResponse = new HashMap<>();
        loginResponse.put("status", HttpServletResponse.SC_OK);
        loginResponse.put("message", "Login successful");

        Map<String, String> data = new HashMap<>();
        data.put("email", savedUser.getMemberEmail());
        data.put("name", savedUser.getMemberName());
        data.put("token", token);

        loginResponse.put("data", data);

        // JSON으로 변환 후 응답
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(loginResponse);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);

        return "redirect:/";
    }

}
