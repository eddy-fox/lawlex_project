package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.soldesk.team_project.dto.ChatRoomDTO;
import com.soldesk.team_project.dto.ChatdataDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.service.ChatdataService;
import com.soldesk.team_project.service.ChatroomService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat/messages") // ← /chat/api/messages 대신 여기로 확정
@RequiredArgsConstructor
public class ChatdataController {

    private final ChatdataService chatDataService;
    private final ChatroomService chatroomService;

    private static class Sender {
        final String type; // "MEMBER" or "LAWYER"
        final Integer id;
        Sender(String t, Integer i) { this.type = t; this.id = i; }
    }
    private Sender resolveSender(@SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
                                 @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer) {
        if (loginMember != null) return new Sender("MEMBER", loginMember.getMemberIdx());
        if (loginLawyer != null) return new Sender("LAWYER", loginLawyer.getLawyerIdx());
        return null;
    }

    /** 텍스트+첨부 혼합 전송 (멀티파트) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendMessage(
            @RequestPart("roomId") Integer roomId,
            @RequestPart(name = "content", required = false) String content,
            @RequestPart(name = "files",   required = false) List<MultipartFile> files,
            @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
            @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer,
            HttpServletResponse resp) throws Exception {

        Sender sender = resolveSender(loginMember, loginLawyer);
        if (sender == null) return ResponseEntity.status(403).body("로그인이 필요합니다.");

        ChatRoomDTO room = chatroomService.getRoom(roomId);
        if (room == null) return ResponseEntity.status(404).body("존재하지 않는 채팅방입니다.");

        boolean isMemberOfRoom =
                ("MEMBER".equals(sender.type) && room.getMemberIdx() != null && room.getMemberIdx().equals(sender.id))
             || ("LAWYER".equals(sender.type) && room.getLawyerIdx() != null && room.getLawyerIdx().equals(sender.id));
        if (!isMemberOfRoom) return ResponseEntity.status(403).body("이 채팅방의 참여자가 아닙니다.");

        if (!"ACTIVE".equalsIgnoreCase(room.getState()))
            return ResponseEntity.status(409).body("진행 중이 아닌 채팅방입니다.");
        if (room.getExpiresAt() != null && java.time.LocalDateTime.now().isAfter(room.getExpiresAt()))
            return ResponseEntity.status(409).body("상담 시간이 만료되었습니다.");

        boolean hasContent = (content != null && !content.isBlank());
        boolean hasFiles   = (files != null && !files.isEmpty());
        if (!hasContent && !hasFiles)
            return ResponseEntity.badRequest().body("메시지 내용 또는 파일이 필요합니다.");

        ChatdataDTO saved = chatDataService.sendMessage(
                roomId, sender.type, sender.id, hasContent ? content : null, files);

        return ResponseEntity.ok(saved);
    }

    /** 텍스트 전용 전송(JSON) */
    public static record TextMessageReq(Integer roomId, String content) {}
    @PostMapping(path = "/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendText(
            @RequestBody TextMessageReq req,
            @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
            @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer) throws Exception {
        if (req == null || req.roomId() == null)
            return ResponseEntity.badRequest().body("roomId가 필요합니다.");
        return sendMessage(req.roomId(), req.content(), null, loginMember, loginLawyer, null);
    }

    /** 첨부만 전송(멀티파트) */
    @PostMapping(path = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendFiles(
            @RequestPart("roomId") Integer roomId,
            @RequestPart("files") List<MultipartFile> files,
            @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
            @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer) throws Exception {
        if (files == null || files.isEmpty())
            return ResponseEntity.badRequest().body("업로드할 파일이 없습니다.");
        return sendMessage(roomId, null, files, loginMember, loginLawyer, null);
    }

    /** 메시지 소프트 삭제 */
    @DeleteMapping("/{chatId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Integer chatId,
                                           @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
                                           @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer) {
        Sender sender = resolveSender(loginMember, loginLawyer);
        if (sender == null) return ResponseEntity.status(403).body("로그인이 필요합니다.");
        boolean ok = chatDataService.softDelete(chatId, sender.type, sender.id);
        if (!ok) return ResponseEntity.status(403).body("삭제 권한이 없거나 이미 삭제된 메시지입니다.");
        return ResponseEntity.ok().build();
    }
}
