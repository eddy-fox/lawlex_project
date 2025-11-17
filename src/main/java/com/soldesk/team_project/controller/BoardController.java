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
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;

import com.soldesk.team_project.form.BoardForm;
import com.soldesk.team_project.form.ReBoardForm;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.AdminEntity;
import com.soldesk.team_project.entity.ReBoardEntity;
import com.soldesk.team_project.service.BoardService;
import com.soldesk.team_project.service.MemberService;
import com.soldesk.team_project.service.ReBoardService;
import com.soldesk.team_project.service.CategoryRecommendService;
import com.soldesk.team_project.repository.AdminRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.dto.UserMasterDTO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/board")
@RequiredArgsConstructor
@Controller
public class BoardController {
    
    private final BoardService boardService;
    private final MemberService memberService;
    private final ReBoardService reboardService;
    private final CategoryRecommendService categoryRecommendService;
    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final com.soldesk.team_project.repository.LawyerRepository lawyerRepository;

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
    public String detail(Model model, @PathVariable("id") Integer id, ReBoardForm reboardForm,
                         HttpSession session,
                         @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        model.addAttribute("boardEntity", boardEntity);
        
        // 로그인 사용자 정보 추가
        model.addAttribute("loginUser", loginUser);
        
        // 로그인한 변호사 정보 추가 (프로필 사진용)
        if (loginUser != null && loginUser.getRole() != null && "LAWYER".equals(loginUser.getRole()) && loginUser.getLawyerIdx() != null) {
            lawyerRepository.findById(loginUser.getLawyerIdx()).ifPresent(lawyer -> {
                model.addAttribute("loginLawyer", lawyer);
            });
        }
        
        // 관리자 권한 확인
        AdminEntity loginAdmin = getLoginAdmin(session);
        boolean isAdmin = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        model.addAttribute("isAdmin", isAdmin);
        
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
    public String boardCrete(@Valid BoardForm boardForm, BindingResult bindingResult,
                             @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                             HttpSession session) {

        if(bindingResult.hasErrors()) {
            return "board/write";
        }
        
        // 세션에서 사용자 정보 가져오기
        if (loginUser == null || loginUser.getMemberIdx() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        MemberEntity memberEntity = memberRepository.findById(loginUser.getMemberIdx())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
        
        this.boardService.create(
            boardForm.getBoardTitle(), 
            boardForm.getBoardContent(), 
            boardForm.getBoardCategory(),
            boardForm.getInterestIdx(),
            memberEntity
        );
        
        // 작성한 글의 interestIdx에 해당하는 리스트로 리다이렉트
        // 카테고리로부터 interestIdx 자동 결정
        Integer interestIdx = boardForm.getInterestIdx();
        if (interestIdx == null || interestIdx <= 0) {
            interestIdx = boardService.getInterestIdxFromCategory(boardForm.getBoardCategory());
        }
        return "redirect:/board/list?interestIdx=" + interestIdx;

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String boardModifyForm(BoardForm boardForm, Principal principal, 
                                  @PathVariable("id") Integer id, HttpSession session) {
        
        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        
        // 관리자 권한 확인
        AdminEntity loginAdmin = getLoginAdmin(session);
        boolean isAdmin = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        
        // 작성자 확인 (Principal이 있을 때만)
        boolean isOwner = false;
        if (principal != null && boardEntity.getMember() != null) {
            isOwner = boardEntity.getMember().getMemberId().equals(principal.getName());
        }
        
        if(!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        
        boardForm.setBoardTitle(boardEntity.getBoardTitle());
        boardForm.setBoardContent(boardEntity.getBoardContent());
        boardForm.setBoardCategory(boardEntity.getBoardCategory());
        if (boardEntity.getInterest() != null) {
            boardForm.setInterestIdx(boardEntity.getInterest().getInterestIdx());
        }
        
        return "board/write";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String boardModify(@Valid BoardForm boardForm, BindingResult bindingResult,
    Principal principal, @PathVariable("id") Integer id, HttpSession session) {
        
        if(bindingResult.hasErrors()) {
            return "board/write";
        }
        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        
        // 관리자 권한 확인
        AdminEntity loginAdmin = getLoginAdmin(session);
        boolean isAdmin = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        
        // 작성자 확인 (Principal이 있을 때만)
        boolean isOwner = false;
        if (principal != null && boardEntity.getMember() != null) {
            isOwner = boardEntity.getMember().getMemberId().equals(principal.getName());
        }
        
        if(!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        
        this.boardService.modify(boardEntity, boardForm.getBoardTitle(), boardForm.getBoardContent());
        return String.format("redirect:/board/detail/%s", id);

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String boardDelete(Principal principal, @PathVariable("id") Integer id, HttpSession session) {

        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        
        // 관리자 권한 확인
        AdminEntity loginAdmin = getLoginAdmin(session);
        boolean isAdmin = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        
        // 작성자 확인 (Principal이 있을 때만)
        boolean isOwner = false;
        if (principal != null && boardEntity.getMember() != null) {
            isOwner = boardEntity.getMember().getMemberId().equals(principal.getName());
        }
        
        if(!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
        }
        
        this.boardService.delete(boardEntity);
        return "redirect:/board/list";

    }

    /* ================ 세션 → 엔티티 변환 헬퍼 ================ */
    private AdminEntity getLoginAdmin(HttpSession session) {
        Object obj = session.getAttribute("loginAdmin");
        if (obj == null) return null;

        if (obj instanceof AdminEntity ae) {
            return ae;
        }
        if (obj instanceof com.soldesk.team_project.controller.MemberController.AdminSession as) {
            return adminRepository.findById(as.getAdminIdx()).orElse(null);
        }
        return null;
    }

}