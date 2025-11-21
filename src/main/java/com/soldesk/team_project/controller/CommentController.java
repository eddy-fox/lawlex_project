package com.soldesk.team_project.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.soldesk.team_project.controller.MemberController.AdminSession;
import com.soldesk.team_project.controller.MemberController.LawyerSession;
import com.soldesk.team_project.controller.MemberController.MemberSession;
import com.soldesk.team_project.dto.CommentDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.entity.AdminEntity;
import com.soldesk.team_project.entity.CommentEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.AdminRepository;
import com.soldesk.team_project.service.CommentService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    
    private final CommentService commentService;
    private final AdminRepository adminRepository;

    // 댓글 작성 요청 DTO
    public static class CommentCreateRequest {
        public Integer newsIdx;
        public String commentContent;
    }

    // 댓글 수정 요청 DTO
    public static class CommentUpdateRequest {
        public String commentContent;
    }

    // 댓글 목록 조회
    @GetMapping("/list/{newsIdx}")
    @ResponseBody
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable("newsIdx") Integer newsIdx) {
        List<CommentDTO> comments = commentService.getCommentsByNewsIdx(newsIdx);
        return ResponseEntity.ok(comments);
    }

    // 댓글 작성
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestBody CommentCreateRequest request,
            @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            CommentDTO comment;
            
            // loginUser에서 memberIdx 또는 lawyerIdx 읽기
            if (loginUser != null && loginUser.getMemberIdx() != null) {
                // 일반 회원이 댓글 작성
                comment = commentService.create(request.newsIdx, loginUser.getMemberIdx(), request.commentContent);
            } else if (loginUser != null && loginUser.getLawyerIdx() != null) {
                // 변호사가 댓글 작성
                comment = commentService.createByLawyer(request.newsIdx, loginUser.getLawyerIdx(), request.commentContent);
            } else {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            response.put("success", true);
            response.put("comment", comment);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "댓글 작성 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 댓글 수정
    @PostMapping("/modify/{commentIdx}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> modifyComment(
            @PathVariable("commentIdx") Integer commentIdx,
            @RequestBody CommentUpdateRequest request,
            HttpSession session,
            @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            CommentEntity comment = commentService.getComment(commentIdx);
            
            // 권한 확인: 작성자 또는 관리자만 수정 가능
            if (!canModifyOrDelete(comment, session, loginUser)) {
                response.put("success", false);
                response.put("message", "수정 권한이 없습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            commentService.modify(comment, request.commentContent);
            
            response.put("success", true);
            response.put("message", "댓글이 수정되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "댓글 수정 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 댓글 삭제 (소프트 삭제)
    @PostMapping("/delete/{commentIdx}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable("commentIdx") Integer commentIdx,
            HttpSession session,
            @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            CommentEntity comment = commentService.getComment(commentIdx);
            
            // 권한 확인: 작성자 또는 관리자만 삭제 가능
            if (!canModifyOrDelete(comment, session, loginUser)) {
                response.put("success", false);
                response.put("message", "삭제 권한이 없습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            commentService.delete(comment);
            
            response.put("success", true);
            response.put("message", "댓글이 삭제되었습니다.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "댓글 삭제 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 수정/삭제 권한 확인
    private boolean canModifyOrDelete(CommentEntity comment, HttpSession session, UserMasterDTO loginUser) {
        // 관리자 권한 확인 (admin_role이 "admin"인 관리자만)
        AdminEntity loginAdmin = getLoginAdmin(session);
        if (loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole())) {
            return true;
        }
        
        // 작성자 권한 확인 (일반 회원)
        if (loginUser != null && loginUser.getMemberIdx() != null
                && commentService.isCommentOwnerByMember(comment, loginUser.getMemberIdx())) {
            return true;
        }
        MemberSession loginMember = getLoginMember(session);
        if (loginMember != null && commentService.isCommentOwnerByMember(comment, loginMember.memberIdx)) {
            return true;
        }
        
        // 작성자 권한 확인 (변호사)
        if (loginUser != null && loginUser.getLawyerIdx() != null
                && commentService.isCommentOwnerByLawyer(comment, loginUser.getLawyerIdx())) {
            return true;
        }
        LawyerSession loginLawyer = getLoginLawyer(session);
        if (loginLawyer != null && commentService.isCommentOwnerByLawyer(comment, loginLawyer.lawyerIdx)) {
            return true;
        }
        
        return false;
    }

    // 세션에서 로그인한 회원 정보 가져오기
    private MemberSession getLoginMember(HttpSession session) {
        Object obj = session.getAttribute("loginMember");
        if (obj == null) return null;
        
        if (obj instanceof MemberSession ms) {
            return ms;
        }
        return null;
    }

    // 세션에서 로그인한 변호사 정보 가져오기
    private LawyerSession getLoginLawyer(HttpSession session) {
        Object obj = session.getAttribute("loginLawyer");
        if (obj == null) return null;
        
        if (obj instanceof LawyerSession ls) {
            return ls;
        }
        if (obj instanceof LawyerEntity le) {
            // LawyerEntity에서 LawyerSession으로 변환
            return new LawyerSession(
                    le.getLawyerIdx(), le.getLawyerId(), le.getLawyerName(),
                    le.getLawyerEmail(), le.getLawyerPhone(), le.getInterestIdx());
        }
        return null;
    }

    // 세션에서 로그인한 관리자 정보 가져오기
    private AdminEntity getLoginAdmin(HttpSession session) {
        Object obj = session.getAttribute("loginAdmin");
        if (obj == null) return null;

        if (obj instanceof AdminEntity ae) {
            return ae;
        }
        if (obj instanceof AdminSession as) {
            return adminRepository.findById(as.adminIdx).orElse(null);
        }
        return null;
    }
}

