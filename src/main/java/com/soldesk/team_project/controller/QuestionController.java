package com.soldesk.team_project.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.soldesk.team_project.dto.AdminDTO;
import com.soldesk.team_project.dto.AnswerDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.service.AdminService;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.QuestionService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final MemberService memberService;
    private final LawyerService lawyerService;
    private final AdminService adminService;

    @GetMapping("/faq")
    public String faq() {
        return "question/faq";
    }

    @GetMapping("/qnaList")
    public String qnaList(@RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "mine", defaultValue = "false" ) boolean mine,
                          @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                            Model model) {

        Page<QuestionDTO> paging;

        if(mine && loginUser != null){
            Integer mIdx = loginUser.getMemberIdx();
            Integer lIdx = loginUser.getLawyerIdx();
            if(mine && mIdx!= null){ /* 일반회원이 확인 */
                paging = questionService.getQnaPagingM(mIdx, page);
            }else if(mine && lIdx != null){ /* 변호사회원 확인 */
                paging = questionService.getQnaPagingL(lIdx, page);
            }else {
                paging = questionService.getQnaPaging(page);
            }
        }else{ paging = questionService.getQnaPaging(page); }

        model.addAttribute("qnaPaging", paging);
        model.addAttribute("mine", mine);
       
        if(loginUser != null){
            model.addAttribute("loginUser", loginUser);
            if(loginUser.getMemberIdx() != null){
                model.addAttribute("myIdxM", loginUser.getMemberIdx());
            }
            if(loginUser.getLawyerIdx() != null) {
                model.addAttribute("myIdxL", loginUser.getLawyerIdx());
            }
        }

        return "question/qnaList";
    }
    
    @GetMapping("/qnaWrite")
    public String qnaWrite(@ModelAttribute("qnaWrite") QuestionDTO qnaWrite,
        @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser){

        if(loginUser == null){ return "redirect:/member/login"; }
        System.out.println("\n"+ qnaWrite.toString() + "\n");
        return "question/qnaWrite";
    }
    
    @PostMapping("/qnaWrite")
    public String qnaWriteSubmit(@ModelAttribute("qnaWrite") QuestionDTO qnaWrite,
                                @SessionAttribute("loginUser") UserMasterDTO loginUser) {
        
        if(loginUser.getMemberIdx() != null ) {
            qnaWrite.setMemberIdx(loginUser.getMemberIdx());
        }
        if(loginUser.getLawyerIdx() != null) {
            qnaWrite.setLawyerIdx(loginUser.getLawyerIdx());
        }

        questionService.qnaWriting(qnaWrite);
        System.out.println("\n"+ qnaWrite.toString() + "\n");
        return "redirect:/question/qnaList";
    }

    // 문의글 상세
    @GetMapping("/qnaInfo")
    public String qnaInfo(@RequestParam("qIdx") int qIdx, Model model, 
                            @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                            RedirectAttributes redirectAttributes) {

        QuestionDTO infoQ = questionService.getQ(qIdx);
        // if(infoQ == null) return "redirect:"; // null 이면 돌아가라
        
        Integer adminIdx = null;
        Integer userIdx = null;

        // 로그인 되어 있을 때만 세션 정보 가져오기
        if (loginUser != null) {
            adminIdx = loginUser.getAdminIdx();
            userIdx = loginUser.getMemberIdx() != null ? loginUser.getMemberIdx() : loginUser.getLawyerIdx();

            if (adminIdx != null) {
                AdminDTO admin = adminService.searchSessionAdmin(adminIdx);
                model.addAttribute("admin", admin);
            }
        }

        // 비밀글 조회 권환 확인
        int secret = infoQ.getQSecret();
        Integer writerIdx = infoQ.getMemberIdx() != null ? infoQ.getMemberIdx() : infoQ.getLawyerIdx();

        if (secret == 1 && adminIdx == null && writerIdx != null && userIdx != null && !userIdx.equals(writerIdx)) {
            redirectAttributes.addFlashAttribute("alert", "비밀글은 작성자와 관리자만 열람할 수 있습니다.");

            return "redirect:/question/qnaList";
        }

        Integer mIdx = infoQ.getMemberIdx();
        Integer lIdx = infoQ.getLawyerIdx();

        if (lIdx != null) {
            LawyerDTO l = lawyerService.qLawyerInquiry(lIdx);
            infoQ.setInfoId(l.getLawyerId());
            infoQ.setInfoName(l.getLawyerName());
        }else if (mIdx != null) {
            MemberDTO m = memberService.qMemberInquiry(mIdx);
            infoQ.setInfoId(m.getMemberId());
            infoQ.setInfoName(m.getMemberName());
        }
        model.addAttribute("infoQ", infoQ);

        // 답변받을 템플릿 전달
        if (infoQ.getQAnswer() == 1) {
            AnswerDTO questionAnswer = questionService.getAnswerToQIdx(infoQ.getQIdx());
            model.addAttribute("questionAnswer", questionAnswer);
        } else {
            AnswerDTO answerWrite = new AnswerDTO();
            answerWrite.setQIdx(infoQ.getQIdx());
            answerWrite.setAdminIdx(adminIdx);
            model.addAttribute("answerWrite", answerWrite);
        }

        return "question/qnaInfo";
    }

    // 문의글 답변 등록
    @PostMapping("/answerWrite")
    public String answerSubmit(@ModelAttribute("answerWrite") AnswerDTO answerWrite,
                                RedirectAttributes redirectAttributes) {
                                    
        questionService.answerProcess(answerWrite);

        redirectAttributes.addAttribute("qIdx", answerWrite.getQIdx());

        return "redirect:/question/qnaInfo";
    }

    // 답변 수정
    @GetMapping("/answerModify")
    public String answerModifyForm(@RequestParam("aIdx") Integer aIdx, Model model, RedirectAttributes redirectAttributes, 
                                    @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        QuestionDTO question = questionService.getQuestionToAIdx(aIdx);
        
        // 관리자 세션 확인
        Integer adminIdx = loginUser.getAdminIdx();
        if (adminIdx == null) {
            redirectAttributes.addFlashAttribute("alert", "세션이 만료되었습니다.");
            redirectAttributes.addAttribute("qIdx", question.getQIdx());

            return "redirect:/question/qnaInfo";
        }

        AnswerDTO answerModify = questionService.getAnswerToAIdx(aIdx);
        model.addAttribute("answerModify", answerModify);
        model.addAttribute("question", question);

        redirectAttributes.addAttribute("aIdx", aIdx);
        
        return "question/answerModify";
    }
    @PostMapping("/answerModify")
    public String answerModifySubmit(@ModelAttribute("answerModify") AnswerDTO answerModify,
                                    RedirectAttributes redirectAttributes) {

        questionService.modifyAnswer(answerModify);

        redirectAttributes.addAttribute("qIdx", answerModify.getQIdx());

        return "redirect:/question/qnaInfo";
    }

    // 답변 삭제
    @GetMapping("/answerDelete")
    public String answerDelete(@RequestParam("aIdx") Integer aIdx, RedirectAttributes redirectAttributes,
                                @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        QuestionDTO question = questionService.getQuestionToAIdx(aIdx);
        Integer qIdx = question.getQIdx();
        
        // 관리자 세션 확인
        Integer adminIdx = loginUser.getAdminIdx();
        if (adminIdx == null) {
            redirectAttributes.addFlashAttribute("alert", "세션이 만료되었습니다.");
            redirectAttributes.addAttribute("qIdx", qIdx);

            return "redirect:/question/qnaInfo";
        }

        questionService.deleteAnswer(aIdx);

        redirectAttributes.addFlashAttribute("alert", "답변이 삭제되었습니다.");
        redirectAttributes.addAttribute("qIdx", qIdx);

        return "redirect:/question/qnaInfo";
    }

}