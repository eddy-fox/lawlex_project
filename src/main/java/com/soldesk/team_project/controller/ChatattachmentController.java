package com.soldesk.team_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.entity.ChatAttachmentEntity;
import com.soldesk.team_project.service.ChatroomService;
import com.soldesk.team_project.repository.ChatattachmentRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/chat/attachment")
@RequiredArgsConstructor
public class ChatattachmentController {

    private final ChatattachmentRepository attachmentRepository;
    private final ChatroomService chatroomService;

    /**
     * 보안 프록시:
     * - 첨부 ID로 조회 → 해당 채팅방의 "참여자"인지 확인 → 실제 저장소 URL로 302 리다이렉트
     * - 직접 file_url 노출을 피하고 권한을 서버에서 보장하고 싶을 때 사용
     */
    @GetMapping("/{attachmentId}")
    public void redirectToFile(@PathVariable Integer attachmentId,
                               @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
                               @SessionAttribute(value = "loginLawyer",  required = false) LawyerDTO loginLawyer,
                               HttpServletResponse resp) throws Exception {
        ChatAttachmentEntity att = attachmentRepository.findById(attachmentId).orElse(null);
        if (att == null || (att.getActive() != null && att.getActive() == 0)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var msg  = att.getChatData();
        var room = (msg != null) ? msg.getChatroom() : null;
        if (room == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        boolean allowed = chatroomService.isParticipant(
                room.getChatroomIdx(),
                loginMember != null ? loginMember.getMemberIdx() : null,
                loginLawyer  != null ? loginLawyer.getLawyerIdx()  : null
        );
        if (!allowed) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        resp.sendRedirect(att.getFileUrl());
    }

    /**
     * (선택) 첨부 소프트삭제: 메시지 작성자만 가능
     * 프런트에서 XHR로 호출하여 첨부 숨기기 처리할 때 사용
     */
    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<?> delete(@RequestParam Integer attachmentId,
                                    @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
                                    @SessionAttribute(value = "loginLawyer",  required = false) LawyerDTO loginLawyer) {
        var att = attachmentRepository.findById(attachmentId).orElse(null);
        if (att == null) return ResponseEntity.notFound().build();

        var msg = att.getChatData();
        // 작성자 확인
        boolean isOwner =
            msg != null &&
            msg.getSenderType() != null &&
            ((msg.getSenderType().equalsIgnoreCase("MEMBER") && loginMember != null && msg.getSenderId().equals(loginMember.getMemberIdx()))
          || (msg.getSenderType().equalsIgnoreCase("LAWYER")  && loginLawyer  != null && msg.getSenderId().equals(loginLawyer.getLawyerIdx())));

        if (!isOwner) return ResponseEntity.status(403).build();

        att.setActive(0);
        attachmentRepository.save(att);
        return ResponseEntity.ok().build();
    }
}
