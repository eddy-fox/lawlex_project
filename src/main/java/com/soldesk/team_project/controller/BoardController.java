package com.soldesk.team_project.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.soldesk.team_project.service.FirebaseStorageService;
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
    private final FirebaseStorageService storageService;
    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final com.soldesk.team_project.repository.LawyerRepository lawyerRepository;
    private final com.soldesk.team_project.repository.BoardRepository boardRepository;
    private final com.soldesk.team_project.repository.InterestRepository interestRepository;

    @GetMapping("/main")
    public String main(Model model,
                       HttpSession session,
                       @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                       @SessionAttribute(value = "loginMember", required = false) MemberController.MemberSession loginMember,
                       @SessionAttribute(value = "loginLawyer", required = false) MemberController.LawyerSession loginLawyer) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        if (loginUser != null && "MEMBER".equalsIgnoreCase(loginUser.getRole()) && loginMember != null) {
            // 로그인한 일반회원: 관심분야별로 각각 5개씩
            java.util.List<java.util.Map<String, Object>> interestBoardsList = new java.util.ArrayList<>();
            
            if (loginMember.interestIdx1 != null) {
                org.springframework.data.domain.Pageable pageable5 = org.springframework.data.domain.PageRequest.of(0, 5);
                java.util.List<BoardEntity> boards1 = boardRepository.findTop5ActiveBoardsBySingleInterestIdxOrderByBoardViewsDesc(
                    loginMember.interestIdx1, pageable5);
                com.soldesk.team_project.entity.InterestEntity interest1 = interestRepository.findById(loginMember.interestIdx1).orElse(null);
                if (interest1 != null && !boards1.isEmpty()) {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("interestName", interest1.getInterestName());
                    map.put("interestIdx", loginMember.interestIdx1);
                    map.put("boards", boards1);
                    interestBoardsList.add(map);
                }
            }
            
            if (loginMember.interestIdx2 != null) {
                org.springframework.data.domain.Pageable pageable5 = org.springframework.data.domain.PageRequest.of(0, 5);
                java.util.List<BoardEntity> boards2 = boardRepository.findTop5ActiveBoardsBySingleInterestIdxOrderByBoardViewsDesc(
                    loginMember.interestIdx2, pageable5);
                com.soldesk.team_project.entity.InterestEntity interest2 = interestRepository.findById(loginMember.interestIdx2).orElse(null);
                if (interest2 != null && !boards2.isEmpty()) {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("interestName", interest2.getInterestName());
                    map.put("interestIdx", loginMember.interestIdx2);
                    map.put("boards", boards2);
                    interestBoardsList.add(map);
                }
            }
            
            if (loginMember.interestIdx3 != null) {
                org.springframework.data.domain.Pageable pageable5 = org.springframework.data.domain.PageRequest.of(0, 5);
                java.util.List<BoardEntity> boards3 = boardRepository.findTop5ActiveBoardsBySingleInterestIdxOrderByBoardViewsDesc(
                    loginMember.interestIdx3, pageable5);
                com.soldesk.team_project.entity.InterestEntity interest3 = interestRepository.findById(loginMember.interestIdx3).orElse(null);
                if (interest3 != null && !boards3.isEmpty()) {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("interestName", interest3.getInterestName());
                    map.put("interestIdx", loginMember.interestIdx3);
                    map.put("boards", boards3);
                    interestBoardsList.add(map);
                }
            }
            
            model.addAttribute("interestBoardsList", interestBoardsList);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("pageTitle", "내 관심분야 인기글");
        } else if (loginUser != null && "LAWYER".equalsIgnoreCase(loginUser.getRole()) && loginLawyer != null && loginLawyer.interestIdx != null) {
            // 로그인한 변호사: 관심분야 1개에 대해 상위 5개
            java.util.List<java.util.Map<String, Object>> interestBoardsList = new java.util.ArrayList<>();
            
            org.springframework.data.domain.Pageable pageable5 = org.springframework.data.domain.PageRequest.of(0, 5);
            java.util.List<BoardEntity> boards = boardRepository.findTop5ActiveBoardsBySingleInterestIdxOrderByBoardViewsDesc(
                loginLawyer.interestIdx, pageable5);
            com.soldesk.team_project.entity.InterestEntity interest = interestRepository.findById(loginLawyer.interestIdx).orElse(null);
            if (interest != null && !boards.isEmpty()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("interestName", interest.getInterestName());
                map.put("interestIdx", loginLawyer.interestIdx);
                map.put("boards", boards);
                interestBoardsList.add(map);
            }
            
            model.addAttribute("interestBoardsList", interestBoardsList);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("pageTitle", "내 관심분야 인기글");
        } else {
            // 로그인하지 않은 회원: 모든 글 중 조회수 높은 순서 10개
            java.util.List<BoardEntity> topBoards = boardRepository.findTop10ActiveBoardsByOrderByBoardViewsDesc(pageable);
            model.addAttribute("topBoards", topBoards);
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("pageTitle", "현재 인기많은 글");
        }
        
        model.addAttribute("loginUser", loginUser);
        // 사이드바에 interestIdx를 전달하지 않음 (강조 없이 표시)
        return "board/main";
    }

    @GetMapping("/list")
    public String list(Model model, 
    @RequestParam(value="page", defaultValue="0") int page,
    @RequestParam(value="kw", defaultValue="") String kw,
    @RequestParam(value="interestIdx", required = false) Integer interestIdx,
    HttpSession session,
    @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        System.out.println("[DEBUG] BoardController.list - 요청받은 interestIdx: " + interestIdx);
        
        if(interestIdx == null) {
            interestIdx = 1;
            System.out.println("[DEBUG] BoardController.list - interestIdx가 null이어서 기본값 1로 설정");
        }
        
        System.out.println("[DEBUG] BoardController.list - 최종 사용할 interestIdx: " + interestIdx);

        Page<BoardEntity> paging;
       
        paging = this.boardService.getListByInterest(page, kw, interestIdx);
   
        // 페이징 범위 계산 (10개씩 표시)
        int currentBlock = page / 10;
        int startPage = currentBlock * 10;
        int endPage = Math.min(startPage + 9, paging.getTotalPages() - 1);
        
        // 관리자 권한 확인
        AdminEntity loginAdmin = getLoginAdmin(session);
        boolean isAdmin = loginAdmin != null;
        model.addAttribute("isAdmin", isAdmin);
        
        // 로그인 사용자 정보 추가
        model.addAttribute("loginUser", loginUser);
        
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("interestIdx", interestIdx);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        return "board/list";

    }

    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, ReBoardForm reboardForm,
                         HttpSession session,
                         @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        model.addAttribute("boardEntity", boardEntity);
        
        // interestIdx 추가 (사이드메뉴 강조 표시용)
        Integer interestIdx = null;
        if (boardEntity.getInterest() != null) {
            interestIdx = boardEntity.getInterest().getInterestIdx();
        }
        if (interestIdx == null) {
            interestIdx = 1; // 기본값
        }
        model.addAttribute("interestIdx", interestIdx);
        
        // 로그인 사용자 정보 추가
        model.addAttribute("loginUser", loginUser);
        
        // 로그인한 변호사 정보 추가 (프로필 사진용)
        if (loginUser != null && loginUser.getRole() != null && "LAWYER".equals(loginUser.getRole()) && loginUser.getLawyerIdx() != null) {
            lawyerRepository.findById(loginUser.getLawyerIdx()).ifPresent(lawyer -> {
                model.addAttribute("loginLawyer", lawyer);
            });
        }
        
        // 일반회원이 각 답글에 좋아요를 눌렀는지 확인
        java.util.Map<Integer, Boolean> likedReboards = new java.util.HashMap<>();
        if (loginUser != null && "MEMBER".equalsIgnoreCase(loginUser.getRole()) && loginUser.getMemberIdx() != null) {
            if (boardEntity.getReboardList() != null) {
                for (com.soldesk.team_project.entity.ReBoardEntity reboard : boardEntity.getReboardList()) {
                    boolean isLiked = false;
                    // memberVoter 강제 로딩 및 확인
                    if (reboard.getMemberVoter() != null) {
                        // 각 MemberEntity를 실제로 로드
                        for (com.soldesk.team_project.entity.MemberEntity member : reboard.getMemberVoter()) {
                            if (member.getMemberIdx() != null && member.getMemberIdx().equals(loginUser.getMemberIdx())) {
                                isLiked = true;
                                break;
                            }
                        }
                    }
                    likedReboards.put(reboard.getReboardIdx(), isLiked);
                }
            }
        }
        model.addAttribute("likedReboards", likedReboards);
        
        // 세션에서 alreadyLiked 플래그 확인 및 모델에 추가
        boolean alreadyLiked = session.getAttribute("alreadyLiked") != null && (Boolean) session.getAttribute("alreadyLiked");
        model.addAttribute("alreadyLiked", alreadyLiked);
        
        // 세션에서 alreadyLiked 플래그 제거 (한 번만 표시하기 위해)
        if (session.getAttribute("alreadyLiked") != null) {
            session.removeAttribute("alreadyLiked");
        }
        
        // 관리자 권한 확인
        AdminEntity loginAdmin = getLoginAdmin(session);
        boolean isAdmin = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        model.addAttribute("isAdmin", isAdmin);
        
        return "board/reBoard";

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String boardCreate(BoardForm boardForm,
                              @RequestParam(value = "interestIdx", required = false) Integer interestIdx,
                              Model model) {

        if (interestIdx == null || interestIdx <= 0) {
            interestIdx = 1;
        }
        model.addAttribute("interestIdx", interestIdx);
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
                             HttpSession session, Model model) {

        if(bindingResult.hasErrors()) {
            return "board/write";
        }
        
        // 세션에서 사용자 정보 가져오기
        if (loginUser == null || loginUser.getMemberIdx() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        MemberEntity memberEntity = memberRepository.findById(loginUser.getMemberIdx())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
        
        BoardEntity writeBoard = this.boardService.create(
            boardForm.getBoardTitle(), 
            boardForm.getBoardContent(), 
            boardForm.getBoardCategory(),
            boardForm.getInterestIdx(),
            memberEntity
        );
        
        // 이미지 파일 업로드 처리
        if (boardForm.getImgFile() != null && !boardForm.getImgFile().isEmpty()) {
            try {
                String filename = nowUuidName(boardForm.getImgFile().getOriginalFilename());
                String objectPath = "boardimg/" + filename;
                
                var uploaded = storageService.upload(boardForm.getImgFile(), objectPath);
                
                // boardImgPath에 Firebase URL 저장
                writeBoard.setBoardImgPath(uploaded.url());
                // boardImgid에 경로+파일명 저장 (예: /boardimg/파일명)
                writeBoard.setBoardImgid("/" + objectPath);
                
                // 업데이트된 엔티티 저장
                boardService.save(writeBoard);
            } catch (Exception e) {
                // 이미지 업로드 실패 시 무시하고 계속 진행
            }
        }
        
        // GPT 자동 답변 생성
        reboardService.gptAutoReboard(writeBoard);

        // 작성한 글의 interestIdx에 해당하는 리스트로 리다이렉트
        // 저장된 엔티티를 다시 조회하여 interestIdx 확인 (Lazy 로딩 문제 방지)
        Integer interestIdx = null;
        
        // 저장 전에 폼에서 넘어온 interestIdx를 먼저 확인
        if (boardForm.getInterestIdx() != null && boardForm.getInterestIdx() > 0) {
            interestIdx = boardForm.getInterestIdx();
        } else {
            // 폼에서 넘어온 interestIdx가 없으면 저장된 엔티티에서 조회
            BoardEntity savedBoard = boardService.getBoardEntity(writeBoard.getBoardIdx());
            if (savedBoard != null && savedBoard.getInterest() != null) {
                interestIdx = savedBoard.getInterest().getInterestIdx();
            }
        }
        
        // 여전히 interestIdx가 없으면 카테고리로부터 결정
        if (interestIdx == null || interestIdx <= 0) {
            interestIdx = boardService.getInterestIdxFromCategory(boardForm.getBoardCategory());
        }
        
        // 디버깅 로그
        System.out.println("[DEBUG] BoardController.boardCrete - 리다이렉트 interestIdx: " + interestIdx);
        System.out.println("[DEBUG] BoardController.boardCrete - boardForm.interestIdx: " + boardForm.getInterestIdx());
        System.out.println("[DEBUG] BoardController.boardCrete - boardForm.boardCategory: " + boardForm.getBoardCategory());
        
        return "redirect:/board/list?interestIdx=" + interestIdx;
    }
    @GetMapping("/api/check-gpt-answer")
    @ResponseBody
    public Map<String, Object> checkGptAnswer(@RequestParam("boardIdx") Integer boardIdx) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 해당 게시글의 답변이 존재하는지 확인
            ReBoardEntity reboard = reboardService.getReboardByBoardIdx(boardIdx);
            
            if (reboard != null && reboard.getReboardActive() == 1) {
                result.put("answerExists", true);
                result.put("reboardIdx", reboard.getReboardIdx());
            } else {
                result.put("answerExists", false);
            }
        } catch (Exception e) {
            result.put("answerExists", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String boardModifyForm(BoardForm boardForm, 
                                  @PathVariable("id") Integer id, 
                                  HttpSession session,
                                  @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        
        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        
        // 관리자 권한 확인
        AdminEntity loginAdmin = getLoginAdmin(session);
        boolean isAdmin = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        
        // 작성자 확인 (loginUser 사용)
        boolean isOwner = false;
        if (loginUser != null && loginUser.getMemberIdx() != null && boardEntity.getMember() != null) {
            isOwner = loginUser.getMemberIdx().equals(boardEntity.getMember().getMemberIdx());
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
                              @PathVariable("id") Integer id, 
                              HttpSession session,
                              @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        
        if(bindingResult.hasErrors()) {
            return "board/write";
        }
        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        
        // 관리자 권한 확인
        AdminEntity loginAdmin = getLoginAdmin(session);
        boolean isAdmin = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        
        // 작성자 확인 (loginUser 사용)
        boolean isOwner = false;
        if (loginUser != null && loginUser.getMemberIdx() != null && boardEntity.getMember() != null) {
            isOwner = loginUser.getMemberIdx().equals(boardEntity.getMember().getMemberIdx());
        }
        
        if(!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        
        // 제목과 내용 수정
        this.boardService.modify(boardEntity, boardForm.getBoardTitle(), boardForm.getBoardContent());
        
        // 이미지 파일 업로드 처리 (새 이미지가 첨부된 경우)
        if (boardForm.getImgFile() != null && !boardForm.getImgFile().isEmpty()) {
            try {
                // 기존 이미지가 있으면 Firebase에서 삭제
                if (boardEntity.getBoardImgid() != null && !boardEntity.getBoardImgid().isEmpty()) {
                    try {
                        // /boardimg/파일명 형식에서 앞의 / 제거
                        String oldObjectPath = boardEntity.getBoardImgid().startsWith("/") 
                            ? boardEntity.getBoardImgid().substring(1) 
                            : boardEntity.getBoardImgid();
                        storageService.delete(oldObjectPath);
                    } catch (Exception e) {
                        // 삭제 실패해도 계속 진행
                    }
                }
                
                // 새 이미지 업로드
                String filename = nowUuidName(boardForm.getImgFile().getOriginalFilename());
                String objectPath = "boardimg/" + filename;
                
                var uploaded = storageService.upload(boardForm.getImgFile(), objectPath);
                
                // boardImgPath에 Firebase URL 저장
                boardEntity.setBoardImgPath(uploaded.url());
                // boardImgid에 경로+파일명 저장 (예: /boardimg/파일명)
                boardEntity.setBoardImgid("/" + objectPath);
                
                // 업데이트된 엔티티 저장
                boardService.save(boardEntity);
            } catch (Exception e) {
                // 이미지 업로드 실패 시 무시하고 계속 진행
            }
        }
        
        return String.format("redirect:/board/detail/%s", id);

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String boardDelete(@PathVariable("id") Integer id, 
                              HttpSession session,
                              @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {

        BoardEntity boardEntity = this.boardService.getBoardEntity(id);
        
        // 관리자 권한 확인
        AdminEntity loginAdmin = getLoginAdmin(session);
        boolean isAdmin = loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        
        // 작성자 확인 (loginUser 사용)
        boolean isOwner = false;
        if (loginUser != null && loginUser.getMemberIdx() != null && boardEntity.getMember() != null) {
            isOwner = loginUser.getMemberIdx().equals(boardEntity.getMember().getMemberIdx());
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

    /**
     * 파일명 생성 메서드 (Firebase 업로드용)
     * 형식: yyyyMMdd_HHmmssSSS-8자리UUID.확장자
     * 예: 20251113_213015123-7f3a9c1b.jpg
     */
    private String nowUuidName(String originalFilename) {
        String ext = getExt(originalFilename);
        String now = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
        String shortUuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return now + "-" + shortUuid + ext;
    }

    /**
     * 원본 파일명에서 확장자 추출
     */
    private String getExt(String original) {
        if (original == null || original.isBlank()) return ".bin";
        String name = original.trim();
        int i = name.lastIndexOf('.');
        if (i < 0 || i == name.length() - 1) return ".bin";
        String ext = name.substring(i).toLowerCase(java.util.Locale.ROOT);
        if (ext.length() > 10 || ext.contains("/") || ext.contains("\\") || ext.contains(" ")) return ".bin";
        return ext;
    }

}