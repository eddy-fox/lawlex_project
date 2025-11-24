package com.soldesk.team_project.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.soldesk.team_project.dto.AdDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.service.AdService;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.QuestionService;
import com.soldesk.team_project.service.FirebaseStorageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final LawyerService lawyerService;
    private final QuestionService questionService;
    private final AdService adService;
    private final FirebaseStorageService storageService;

    private String nowUuidName(String originalFilename) {
        if (originalFilename == null) originalFilename = "";
        String ext = "";
        int idx = originalFilename.lastIndexOf('.');
        if (idx >= 0 && idx < originalFilename.length() - 1) {
            ext = originalFilename.substring(idx).toLowerCase(); // .jpg 등
        } else {
            ext = ".bin";
        }

        String now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
        String uuid8 = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        return now + "-" + uuid8 + ext;   // 예: 20251113_223512123-1a2b3c4d.jpg
    }

    // 일반 회원 관리
    @GetMapping("/memberManagement")
    public String memberList(
        @RequestParam(value = "keyword",required = false) String keyword, Model model, RedirectAttributes redirectAttributes,
        @RequestParam(value = "searchType", required = false, defaultValue = "idx") String searchType,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        
        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        List<MemberDTO> memberList;

        if (keyword == null || keyword.trim().isEmpty()) {
            // 모든 회원 조회
            memberList = memberService.getAllMember();
        } else {
            // 검색으로 회원 조회
            memberList = memberService.searchMembers(searchType, keyword);
        }

        model.addAttribute("memberList", memberList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        return "admin/memberManagement";
    }

    @PostMapping("/memberManagement")
    public String memberSearch(
        @RequestParam("keyword") String keyword,
        @RequestParam("searchType") String searchType,
        RedirectAttributes redirectAttributes,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("searchType", searchType);
        return "redirect:/admin/memberManagement";
    }

    // 변호사 회원 관리
    @GetMapping("/lawyerManagement")
    public String lawyerList(
        @RequestParam(value = "keyword",required = false) String keyword, Model model, RedirectAttributes redirectAttributes,
        @RequestParam(value = "searchType", required = false, defaultValue = "idx") String searchType,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }
        
        List<LawyerDTO> lawyerList;

        if (keyword == null || keyword.trim().isEmpty()) {
            // 모든 회원 조회
            lawyerList = lawyerService.getAllLawyer();
        } else {
            // 검색으로 회원 조회
            lawyerList = lawyerService.searchLawyers(searchType, keyword);
        }

        model.addAttribute("lawyerList", lawyerList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        return "admin/lawyerManagement";
    }

    @PostMapping("/lawyerManagement")
    public String lawyerSearch(
        @RequestParam("keyword") String keyword,
        @RequestParam("searchType") String searchType,
        RedirectAttributes redirectAttributes,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("searchType", searchType);
        return "redirect:/admin/lawyerManagement";
    }

    // 문의글 관리
    @GetMapping("/QnAManagement")
    public String questionList(
        @RequestParam(value = "keyword",required = false) String keyword, Model model, RedirectAttributes redirectAttributes,
        @RequestParam(value = "searchType", required = false, defaultValue = "idx") String searchType,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        
        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        List<QuestionDTO> newQuestions;
        List<QuestionDTO> completedQuestions;

        if (keyword == null || keyword.trim().isEmpty()) {
            // 새로운, 답변한 문의글 조회
            newQuestions = questionService.getQuestions(0);
            completedQuestions = questionService.getQuestions(1);
        } else {
            // 검색으로 새로운, 답변한 문의글 조회
            newQuestions = questionService.searchQuestions(searchType, keyword, 0);
            completedQuestions = questionService.searchQuestions(searchType, keyword, 1);
        }

        model.addAttribute("newQuestions", newQuestions);
        model.addAttribute("completedQuestions", completedQuestions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        return "admin/QnAManagement";
    }

    @PostMapping("/QnAManagement")
    public String questionSearch(
        @RequestParam("keyword") String keyword,
        @RequestParam("searchType") String searchType,
        RedirectAttributes redirectAttributes,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("searchType", searchType);
        return "redirect:/admin/QnAManagement";
    }

    // 광고 관리
    @GetMapping("/adManagement")
    public String adList(Model model, RedirectAttributes redirectAttributes,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        
        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        // 만료된 광고 비활성화
        adService.refreshActiveAds();

        // 활성 광고 조회
        List<AdDTO> adList = adService.getAllAd();

        model.addAttribute("adList", adList);
        return "admin/adManagement";
    }

    // 광고 등록
    @GetMapping("/adRegistration")
    public String registAdForm(RedirectAttributes redirectAttributes,
        @ModelAttribute("adRegistration")AdDTO adRegistration, 
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        return "admin/adRegistration";
    }

    @PostMapping("/adRegistration")
    public String registAdSubmit(RedirectAttributes redirectAttributes,
        @ModelAttribute("adRegistration")AdDTO adRegistration,
        @RequestParam("imageFile") MultipartFile imageFile,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        
        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        // 광고 이미지 업로드
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = nowUuidName(imageFile.getOriginalFilename());
            String objectPath = "ad/" + filename;  // ✅ 광고는 ad 폴더에

            var uploaded = storageService.upload(imageFile, objectPath);
            // 정책: adImgPath 에 "풀 URL" 저장
            adRegistration.setAdImgPath(uploaded.url());
        }

        // 광고 등록 처리
        adService.registProcess(adRegistration);

        return "redirect:/admin/adManagement";
    }

    // 이름으로 변호사 검색 (광고 등록)
    @GetMapping("/lawyerSearch")
    public String searchLawyer(@RequestParam("lawyerName") String lawyerName, Model model) {

        List<LawyerDTO> lawyerList = lawyerService.searchLawyers("name", lawyerName);

        model.addAttribute("lawyerList", lawyerList);
        return "admin/lawyerSearch";
    }

    // 광고 상세
    @GetMapping("/adInfo")
    public String showAd(@RequestParam("adIdx") Integer adIdx, Model model, RedirectAttributes redirectAttributes,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        AdDTO ad = adService.getAd(adIdx);
        model.addAttribute("ad", ad);

        return "admin/adInfo";
    }

    // 광고 수정
    @GetMapping("/adModify")
    public String modifyForm(@RequestParam("adIdx") Integer adIdx, Model model, RedirectAttributes redirectAttributes,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }
            
        AdDTO modifyAd = adService.getAd(adIdx);
        model.addAttribute("modifyAd", modifyAd);

        return "admin/adModify";
    }

    @PostMapping("/adModify")
    public String modifySubmit(@ModelAttribute("modifyAd") AdDTO modifyAd, RedirectAttributes redirectAttributes,
        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, Model model,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String filename   = nowUuidName(imageFile.getOriginalFilename());
            String objectPath = "ads/" + filename;

            var uploaded = storageService.upload(imageFile, objectPath);
            
            modifyAd.setAdImgPath(uploaded.url());   // 기존 경로 덮어쓰기
        }
        
        adService.modifyProcess(modifyAd);

        return "redirect:/admin/adInfo?adIdx=" + modifyAd.getAdIdx();
    }

    // 광고 삭제
    @GetMapping("/adDelete")
    public String deleteAd(@RequestParam("adIdx") int adIdx, RedirectAttributes redirectAttributes,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        if (loginUser.getAdminIdx() == null) {
            redirectAttributes.addFlashAttribute("alert", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }

        adService.deleteProcess(adIdx);

        return "redirect:/admin/adManagement";
    }

    // 광고 조회수 증가
    @PostMapping("/ad/count")
    @ResponseBody
    public ResponseEntity<Void> adCount(@RequestBody Map<String, Object> payload) {
        Integer adIdx = (Integer) payload.get("adIdx");
        adService.increaseAdViews(adIdx);

        return ResponseEntity.ok().build();
    }

    /*
    @GetMapping("/lawyer/pending")
    public String pendingLawyers(Model model) {

        List<LawyerEntity> list = lawyerService.getPending();

        model.addAttribute("list", list);

        return "admin/lawyer-pending";

    }

    @PostMapping("/lawyer/{idx}/approve")
    public String approveLawyer(@PathVariable Integer idx) {

        lawyerService.approve(idx);

        return "redirect:/admin/lawyer/pending";

    }

    @PostMapping("/lawyer/{idx}/reject")
    public String rejectLawyer(@PathVariable Integer idx) {

        lawyerService.reject(idx);

        return "redirect:/admin/lawyer/pending";
    */
    /* Q 문의글 상세보기 */
    // @GetMapping("/qnaAnswer")
    // public String qnaAnswer(@RequestParam("qIdx") int qIdx, Model model) {
    //     QuestionDTO infoQ = questionService.getQ(qIdx);
    //     // if(infoQ == null) return "redirect:"; // null 이면 돌아가라

    //     Integer mIdx = infoQ.getMemberIdx();
    //     Integer lIdx = infoQ.getLawyerIdx();

    //     if (lIdx != null) {
    //         LawyerDTO l = lawyerService.qLawyerInquiry(lIdx);
    //         infoQ.setInfoId(l.getLawyerId());
    //         infoQ.setInfoName(l.getLawyerName());
    //     } else if (mIdx != null) {
    //         MemberDTO m = memberService.qMemberInquiry(mIdx);
    //         infoQ.setInfoId(m.getMemberId());
    //         infoQ.setInfoName(m.getMemberName());
    //     }
    //     model.addAttribute("infoQ", infoQ);
    //     return "admin/qnaAnswer";
    // }

    // 생성자 변경 없이 사용하기 위해 필드 주입 사용
    // @org.springframework.beans.factory.annotation.Autowired
    // private com.soldesk.team_project.repository.LawyerRepository lawyerRepository;

    /**
     * 대기 중(미승인) 변호사 목록 JSON
     * GET /admin/api/lawyer/pending
     */
    // @GetMapping(value = "/api/lawyer/pending", produces = "application/json;charset=UTF-8")
    // @ResponseBody
    // public ResponseEntity<?> getPendingLawyers() {
    //     var list = lawyerRepository.findAll().stream()
    //             .filter(l -> l.getLawyerAuth() == null || l.getLawyerAuth() == 0)
    //             .map(l -> {
    //                 var m = new java.util.HashMap<String, Object>();
    //                 m.put("lawyerIdx", l.getLawyerIdx());
    //                 m.put("lawyerId", l.getLawyerId());
    //                 m.put("lawyerName", l.getLawyerName());
    //                 m.put("lawyerEmail", l.getLawyerEmail());
    //                 m.put("lawyerPhone", l.getLawyerPhone());
    //                 m.put("interestIdx", l.getInterestIdx());
    //                 return m;
    //             })
    //             .collect(java.util.stream.Collectors.toList());
    //     return ResponseEntity.ok(list);
    // }

    /**
     * 변호사 승인 처리
     * POST /admin/api/lawyer/approve
     * 파라미터: lawyerIdx
     */
    // @PostMapping(value = "/api/lawyer/approve", produces = "text/plain;charset=UTF-8")
    // @ResponseBody
    // @org.springframework.transaction.annotation.Transactional
    // public ResponseEntity<String> approveLawyer(@RequestParam("lawyerIdx") Integer lawyerIdx) {
    //     var opt = lawyerRepository.findById(lawyerIdx);
    //     if (opt.isEmpty()) return ResponseEntity.status(404).body("NOT_FOUND");

    //     var l = opt.get();
    //     l.setLawyerAuth(1); // 승인
    //     lawyerRepository.save(l);
    //     return ResponseEntity.ok("OK");
    // }

    /**
     * 변호사 거절 처리 (선택: -1로 표기)
     * POST /admin/api/lawyer/reject
     * 파라미터: lawyerIdx, reason(선택)
     */
    // @PostMapping(value = "/api/lawyer/reject", produces = "text/plain;charset=UTF-8")
    // @ResponseBody
    // @org.springframework.transaction.annotation.Transactional
    // public ResponseEntity<String> rejectLawyer(
    //         @RequestParam("lawyerIdx") Integer lawyerIdx,
    //         @RequestParam(value = "reason", required = false) String reason) {

    //     var opt = lawyerRepository.findById(lawyerIdx);
    //     if (opt.isEmpty()) return ResponseEntity.status(404).body("NOT_FOUND");

    //     var l = opt.get();
    //     l.setLawyerAuth(-1); // 거절
    //     // 필요 시 reason을 별도 컬럼에 저장하도록 확장 (현재는 로깅만)
    //     System.out.println("[ADMIN] Lawyer rejected. idx=" + lawyerIdx + ", reason=" + reason);
    //     lawyerRepository.save(l);
    //     return ResponseEntity.ok("OK");
    // }
}
