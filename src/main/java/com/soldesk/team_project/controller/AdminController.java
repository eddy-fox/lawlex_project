package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.soldesk.team_project.dto.AdDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.infra.DriveUploader;
import com.soldesk.team_project.service.AdService;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.QuestionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    
    private final MemberService memberService;
    private final LawyerService lawyerService;
    private final QuestionService questionService;
    private final AdService adService;
    private final DriveUploader driveUploader;

    // 일반 회원 관리
    @GetMapping("/memberManagement")
    public String memberList(
        @RequestParam(value = "keyword",required = false) String keyword, Model model, 
        @RequestParam(value = "searchType", required = false, defaultValue = "idx") String searchType) {
        
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
        RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("searchType", searchType);
        return "redirect:/admin/memberManagement";
    }

    // 변호사 회원 관리
    @GetMapping("/lawyerManagement")
    public String lawyerList(
        @RequestParam(value = "keyword",required = false) String keyword, Model model, 
        @RequestParam(value = "searchType", required = false, defaultValue = "idx") String searchType) {
        
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
        RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("searchType", searchType);
        return "redirect:/admin/lawyerManagement";
    }

    // 문의글 관리
    @GetMapping("/QnAManagement")
    public String questionList(
        @RequestParam(value = "keyword",required = false) String keyword, Model model, 
        @RequestParam(value = "searchType", required = false, defaultValue = "idx") String searchType) {
        
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
        RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("keyword", keyword);
        redirectAttributes.addAttribute("searchType", searchType);
        return "redirect:/admin/QnAManagement";
    }

    // 광고 관리
    @GetMapping("/adManagement")
    public String adList(Model model) {
        
        // 만료된 광고 비활성화
        adService.refreshActiveAds();

        // 활성 광고 조회
        List<AdDTO> adList = adService.getAllAd();

        model.addAttribute("adList", adList);
        return "admin/adManagement";
    }

    // 광고 등록
    @GetMapping("/adRegistration")
    public String registAdForm(@ModelAttribute("adRegistration")AdDTO adRegistration) {
        return "admin/adRegistration";
    }
    @PostMapping("/adRegistration")
    public String registAdSubmit(
        @ModelAttribute("adRegistration")AdDTO adRegistration,
        @RequestParam("imageFile") MultipartFile imageFile) {
        
        // 광고 이미지 업로드
        String desiredName = adRegistration.getAdImgPath();

        if (imageFile != null && !imageFile.isEmpty()) {
            String parentFolderId = "YOUR_DRIVE_FOLDER_ID";
            // DriveUploader.UploadedFileInfo uploaded = driveUploader.upload(imageFile, parentFolderId, desiredName);
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
    public String showAd(@RequestParam("adIdx") Integer adIdx, Model model) {
        AdDTO ad = adService.getAd(adIdx);
        model.addAttribute("ad", ad);

        return "admin/adInfo";
    }

    // 광고 수정
    @GetMapping("/adModify")
    public String modifyForm(@RequestParam("adIdx") Integer adIdx, Model model) {
        AdDTO modifyAd = adService.getAd(adIdx);
        model.addAttribute("modifyAd", modifyAd);

        return "admin/adModify";
    }
    @PostMapping("/adModify")
    public String modifySubmit(@ModelAttribute("modifyAd") AdDTO modifyAd, Model model) {
        adService.modifyProcess(modifyAd);

        return "redirect:/admin/adInfo?adIdx=" + modifyAd.getAdIdx();
    }

    // 광고 삭제
    @GetMapping("/adDelete")
    public String deleteAd(@RequestParam("adIdx") int adIdx) {
        adService.deleteProcess(adIdx);

        return "redirect:/admin/adManagement";
    }

    
    @GetMapping("/lawyer/pending")
    public String pendingLawyers(Model model) {
        List<LawyerEntity> list = lawyerService.getPending();
        model.addAttribute("list", list);
        return "admin/lawyer-pending";
    }

    // @PostMapping("/lawyer/{idx}/approve")
    // public String approveLawyer(@PathVariable Integer idx) {
    //     lawyerService.approve(idx);
    //     return "redirect:/admin/lawyer/pending";
    // }

    @PostMapping("/lawyer/{idx}/reject")
    public String rejectLawyer(@PathVariable Integer idx) {
        lawyerService.reject(idx);
        return "redirect:/admin/lawyer/pending";
    }
    
}
