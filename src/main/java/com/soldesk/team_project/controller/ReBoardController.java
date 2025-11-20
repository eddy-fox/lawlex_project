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
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.ReBoardEntity;
import com.soldesk.team_project.form.ReBoardForm;
import com.soldesk.team_project.service.BoardService;
import com.soldesk.team_project.service.LawyerService;
import com.soldesk.team_project.service.ReBoardService;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.ReBoardRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.dto.UserMasterDTO;
import org.springframework.web.bind.annotation.SessionAttribute;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("/reboard")
@RequiredArgsConstructor
@Controller
public class ReBoardController {

    private final BoardService boardService;
    private final ReBoardService reboardService;
    private final LawyerService lawyerService;
    private final MemberRepository memberRepository;
    private final ReBoardRepository reboardRepository;
    private final LawyerRepository lawyerRepository;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createReboard(Model model,
                                @PathVariable("id") Integer id,
                                @Valid ReBoardForm reboardForm,
                                BindingResult bindingResult,
                                @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        
        // 변호사 권한 확인
        if (loginUser == null || !"LAWYER".equalsIgnoreCase(loginUser.getRole()) || loginUser.getLawyerIdx() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "변호사만 답변을 작성할 수 있습니다.");
        }
        
        LawyerEntity lawyerEntity = this.lawyerRepository.findById(loginUser.getLawyerIdx())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "변호사 정보를 찾을 수 없습니다."));
        
        if (bindingResult.hasErrors()) {
            // 에러 났을 때는 다시 원래 게시글 정보 보여줘야 하니까 이걸 넣어야 함
            model.addAttribute("boardEntity", boardEntity);
            return "redirect:/board/detail/" + id;
        }
        
        ReBoardEntity reboardEntity = this.reboardService.create(boardEntity, reboardForm.getReboardContent(), lawyerEntity);
        return String.format("redirect:/board/detail/%s#reboard_%s", reboardEntity.getBoardEntity().getBoardIdx(), reboardEntity.getReboardIdx());
    
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String reboardModify(ReBoardForm reboardForm,
                                @PathVariable("id") Integer id,
                                Principal principal) {

        ReBoardEntity reboardEntity = this.reboardService.getReboard(id);
        if(!reboardEntity.getLawyer().getLawyerName().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        reboardForm.setReboardContent(reboardEntity.getReboardContent());
        return "answer_form";

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String reboardModify(@Valid ReBoardForm reboardForm,
                                BindingResult bindingResult,
                                @PathVariable("id") Integer id,
                                Principal principal) {

        if (bindingResult.hasErrors()) {
            return "answer_form";
        }
        ReBoardEntity reboardEntity = this.reboardService.getReboard(id);
        if(!reboardEntity.getLawyer().getLawyerName().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        this.reboardService.modify(reboardEntity, reboardForm.getReboardContent());
        return String.format("redirect:/board/detail/%s#reboard_%s",
                reboardEntity.getBoardEntity().getBoardIdx(),
                reboardEntity.getReboardIdx());

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String reboardDelete(Principal principal,
                                @PathVariable("id") Integer id,
                                jakarta.servlet.http.HttpSession session) {

        ReBoardEntity reboardEntity = this.reboardService.getReboard(id);
        
        // 관리자 권한 확인
        com.soldesk.team_project.entity.AdminEntity loginAdmin = 
            (com.soldesk.team_project.entity.AdminEntity) session.getAttribute("loginAdmin");
        boolean isAdmin = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        
        // 작성자이거나 관리자만 삭제 가능
        boolean canDelete = reboardEntity.getLawyer() != null && 
                           reboardEntity.getLawyer().getLawyerId().equals(principal.getName());
        
        if(!canDelete && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
        }
        
        this.reboardService.delete(reboardEntity);
        return String.format("redirect:/board/detail/%s",
                reboardEntity.getBoardEntity().getBoardIdx());
                
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String reboardVote(Principal principal, @PathVariable("id") Integer id) {

        ReBoardEntity reboard = this.reboardService.getReboard(id);
        LawyerEntity lawyer = this.lawyerService.getLawyer(principal.getName());
        this.reboardService.vote(reboard, lawyer);
        return String.format("redirect:/board/detail/%s#reboard_%s", reboard.getBoardEntity().getBoardIdx(), reboard.getReboardIdx());
        
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/member-like/{id}")
    public String reboardMemberLike(@PathVariable("id") Integer id,
                                    @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                                    jakarta.servlet.http.HttpSession session) {

        // 일반회원만 가능
        if (loginUser == null || !"MEMBER".equalsIgnoreCase(loginUser.getRole()) || loginUser.getMemberIdx() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "일반회원만 좋아요를 누를 수 있습니다.");
        }

        ReBoardEntity reboard = this.reboardService.getReboard(id);
        MemberEntity member = this.memberRepository.findById(loginUser.getMemberIdx())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
        
        Integer boardIdx = reboard.getBoardEntity().getBoardIdx();
        
        // 이미 좋아요를 눌렀는지 확인 (memberIdx로 비교)
        if (reboard.getMemberVoter() != null) {
            boolean alreadyLiked = reboard.getMemberVoter().stream()
                .anyMatch(m -> m.getMemberIdx() != null && m.getMemberIdx().equals(member.getMemberIdx()));
            if (alreadyLiked) {
                // 이미 좋아요를 누른 경우 - alert를 위한 플래그 설정
                session.setAttribute("alreadyLiked", true);
                return String.format("redirect:/board/detail/%s#reboard_%s", boardIdx, reboard.getReboardIdx());
            }
        }
        
        // 같은 게시글의 다른 답글에 이미 좋아요를 눌렀는지 확인
        java.util.List<ReBoardEntity> boardReboards = this.reboardRepository.findByBoardEntityBoardIdxAndReboardActive(boardIdx, 1);
        for (ReBoardEntity rb : boardReboards) {
            if (rb.getMemberVoter() != null) {
                boolean alreadyLikedInOtherReboard = rb.getMemberVoter().stream()
                    .anyMatch(m -> m.getMemberIdx() != null && m.getMemberIdx().equals(member.getMemberIdx()));
                if (alreadyLikedInOtherReboard) {
                    // 이미 이 게시글의 다른 답글에 좋아요를 눌렀음
                    session.setAttribute("alreadyLiked", true);
                    return String.format("redirect:/board/detail/%s#reboard_%s", boardIdx, reboard.getReboardIdx());
                }
            }
        }
        
        this.reboardService.memberLike(reboard, member, boardIdx);
        
        return String.format("redirect:/board/detail/%s#reboard_%s", boardIdx, reboard.getReboardIdx());
        
    }

}
