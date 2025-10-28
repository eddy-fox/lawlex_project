package com.soldesk.team_project.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.ReBoardEntity;
import com.soldesk.team_project.form.ReBoardForm;
import com.soldesk.team_project.service.BoardService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.ReBoardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;


@RequestMapping("/reboard")
@RequiredArgsConstructor
@Controller
public class ReBoardController {
    
    private final BoardService boardService;
    private final ReBoardService reboardService;
    private final MemberService memberService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("create/{id}")
    public String createReboard(Model model, @PathVariable("id") Integer id,
    @Valid ReBoardForm reboardForm, BindingResult bindingResult, Principal principal) {

        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        MemberEntity memberEntity = this.memberService.getMember(principal.getName());
        if(bindingResult.hasErrors()) {
            model.addAttribute("boardEntity", memberEntity);
            return "question_detail";
        }
        ReBoardEntity reboardEntity = this.reboardService.create(boardEntity, reboardForm.getReboard_content(), memberEntity);
        return String.format("redirect:/board/detail/%s#reboard_%s", reboardEntity.getBoardEntity().getBoardIdx(), reboardEntity.getReboardIdx());
    
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String reboardModify(ReBoardForm reboardForm, @PathVariable("id") Integer id,
    Principal principal) {

        ReBoardEntity reboardEntity = this.reboardService.getReboard(id);
        if(!reboardEntity.getAuthor().getMemberId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        reboardForm.setReboard_content(reboardEntity.getReboardContent());
        return "answer_form";
        
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String reboardModify(@Valid ReBoardForm reboardForm, BindingResult bindingResult,
    @PathVariable("id") Integer id, Principal principal) {

        if(bindingResult.hasErrors()) {
            return "answer_form";
        }
        ReBoardEntity reboardEntity = this.reboardService.getReboard(id);
        if(!reboardEntity.getAuthor().getMemberId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        this.reboardService.modify(reboardEntity, reboardForm.getReboard_content());
        return String.format("redirect:/board/detail/%s#reboard_%s", reboardEntity.getBoardEntity().getBoardIdx(), reboardEntity.getReboardIdx());
    
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String reboardDelete(Principal principal, @PathVariable("id") Integer id) {

        ReBoardEntity reboardEntity = this.reboardService.getReboard(id);
        if(!reboardEntity.getAuthor().getMemberId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
        }
        this.reboardService.delete(reboardEntity);
        return String.format("redirect:/board/detail/%s", reboardEntity.getBoardEntity().getBoardIdx());

    }

}