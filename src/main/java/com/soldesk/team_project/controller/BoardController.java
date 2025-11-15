package com.soldesk.team_project.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.soldesk.team_project.form.BoardForm;
import com.soldesk.team_project.form.ReBoardForm;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.service.BoardService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.CategoryRecommendService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/board")
@RequiredArgsConstructor
@Controller
public class BoardController {
    
    private final BoardService boardService;
    private final MemberService memberService;
    private final CategoryRecommendService categoryRecommendService;

    @GetMapping("/list")
    public String list(Model model, 
    @RequestParam(value="page", defaultValue="0") int page,
    @RequestParam(value="kw", defaultValue="") String kw,
    @RequestParam(value="interestIdx", required = false) Integer interestIdx) {

        if(interestIdx == null) {
            interestIdx = 1;
        }

        Page<BoardEntity> paging;
       
        paging = this.boardService.getListByInterest(page, kw, interestIdx);
   
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("interestIdx", interestIdx);
        return "board/list";

    }

    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, ReBoardForm reboardForm) {

        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        model.addAttribute("boardEntity", boardEntity);
        return "board/reBoard";

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String boardCreate(BoardForm boardForm) {

        return "board/write";

    }

    /**
     * 제목을 기반으로 카테고리를 추천받는 API
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/recommend-category")
    @ResponseBody
    public java.util.Map<String, Object> recommendCategory(@RequestParam("title") String title) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        if (title == null || title.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "제목을 입력해주세요.");
            result.put("categories", new java.util.ArrayList<>());
            return result;
        }

        List<String> categories = categoryRecommendService.recommendCategories(title.trim(), 5);
        result.put("success", true);
        result.put("categories", categories);
        
        return result;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String boardCrete(@Valid BoardForm boardForm, BindingResult bindingResult, Principal principal) {

        if(bindingResult.hasErrors()) {
            return "board/write";
        }
        MemberEntity memberEntity = this.memberService.getMember(principal.getName());
        this.boardService.create(
            boardForm.getBoardTitle(), 
            boardForm.getBoardContent(), 
            boardForm.getBoardCategory(),
            boardForm.getInterestIdx(),
            memberEntity
        );
        return "redirect:/board/list";

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String boardModify(@Valid BoardForm boardForm, BindingResult bindingResult,
    Principal principal, @PathVariable("id") Integer id) {
        
        if(bindingResult.hasErrors()) {
            return "board/write";
        }
        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        if(!boardEntity.getMember().getMemberId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        this.boardService.modify(boardEntity, boardForm.getBoardTitle(), boardForm.getBoardContent());
        return String.format("redirect:/board/detail/%s", id);

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String boardDelete(Principal principal, @PathVariable("id") Integer id) {

        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        if(!boardEntity.getMember().getMemberId().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
        }
        this.boardService.delete(boardEntity);
        return "redirect:/";

    }

}