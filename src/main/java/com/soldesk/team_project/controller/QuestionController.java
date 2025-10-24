package com.soldesk.team_project.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;

import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.QuestionEntity;
import com.soldesk.team_project.form.QuestionForm;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.QuestionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final MemberService memberService;

    //게시글 리스트
    @GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="0") int page,
    @RequestParam(value = "kw", defaultValue = "") String kw) {
        
        Page<QuestionEntity> paging = this.questionService.getList(page, kw);
        model.addAttribute("paging", paging);
        return "question_list";//question_list.html

    }

    //게시글 상세
    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id) {

        QuestionEntity question = this.questionService.getQuestionEntity(id);
        model.addAttribute("question", question);
        return "question_detail";

    }

    //게시글 작성
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        
        return "question_form";

    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, 
    BindingResult bindingResult, Principal principal) {
        
        if(bindingResult.hasErrors()) {
            return "question_form";
        }
        MemberEntity member = this.memberService.getMember(principal.getName());
        this.questionService.create(questionForm.getTitle(), questionForm.getContent(), member);
        return "redirect:/question/list";

    }

    //게시글 수정
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, 
    BindingResult bindingResult, Principal principal, @PathVariable("id") Integer id) {

        if(bindingResult.hasErrors()) {
            return "question_form";
        }
        QuestionEntity question = this.questionService.getQuestionEntity(id);
        if(!question.getAuthor().getMemberId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"수정 권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getTitle(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);

    }

    //게시글 삭제(보류)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {

        QuestionEntity question = this.questionService.getQuestionEntity(id);
        if(!question.getAuthor().getMemberId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";

    }

    //게시글 좋아요(보류)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questinoVote(Principal principal, @PathVariable("id") Integer id) {

        QuestionEntity question = this.questionService.getQuestionEntity(id);
        MemberEntity member = this.memberService.getMember(principal.getName());
        this.questionService.vote(question, member);
        return String.format("redirect/question/detail/%s", id);
        
    }
}
