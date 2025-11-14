package com.soldesk.team_project.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.soldesk.team_project.dto.ChatdataDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.service.ChatdataService;
import com.soldesk.team_project.service.ChatroomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatroomService chatroomService; // 권한/상태 체크
    private final ChatdataService chatDataService; // 실제 저장

    /** 클라이언트: fetch('/chat/messages', { method:'POST', body: FormData(...) }) */
    @PostMapping(value = "/messages", consumes = "multipart/form-data")
    public ResponseEntity<ChatdataDTO> postMessage(
            @RequestParam("roomId") Integer roomId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @SessionAttribute(value = "loginMember", required = false) Object loginMemberObj,
            @SessionAttribute(value = "loginLawyer",  required = false) Object loginLawyerObj
    ) throws Exception {
        System.out.println("========================================");
        System.out.println("[DEBUG] ChatMessageController.postMessage - START");
        System.out.println("[DEBUG] roomId: " + roomId);
        System.out.println("[DEBUG] content: " + content);
        System.out.println("[DEBUG] files: " + (files != null ? files.length + " files" : "null"));
        System.out.println("========================================");

        String senderType; Integer senderId;
        
        // MemberSession 또는 MemberDTO 처리
        if (loginMemberObj != null) {
            senderType = "MEMBER";
            if (loginMemberObj instanceof MemberDTO) {
                senderId = ((MemberDTO) loginMemberObj).getMemberIdx();
            } else {
                // MemberController$MemberSession 처리 (필드가 public이므로 리플렉션으로 접근)
                try {
                    java.lang.reflect.Field field = loginMemberObj.getClass().getField("memberIdx");
                    senderId = (Integer) field.get(loginMemberObj);
                } catch (Exception ex) {
                    System.out.println("[DEBUG] ChatMessageController.postMessage - error getting memberIdx: " + ex.getMessage());
                    ex.printStackTrace();
                    return ResponseEntity.status(403).body(null);
                }
            }
            System.out.println("[DEBUG] ChatMessageController.postMessage - sender: MEMBER, id: " + senderId);
        }
        // LawyerSession 또는 LawyerDTO 처리
        else if (loginLawyerObj != null) {
            senderType = "LAWYER";
            if (loginLawyerObj instanceof LawyerDTO) {
                senderId = ((LawyerDTO) loginLawyerObj).getLawyerIdx();
            } else {
                // MemberController$LawyerSession 처리 (필드가 public이므로 리플렉션으로 접근)
                try {
                    java.lang.reflect.Field field = loginLawyerObj.getClass().getField("lawyerIdx");
                    senderId = (Integer) field.get(loginLawyerObj);
                } catch (Exception ex) {
                    System.out.println("[DEBUG] ChatMessageController.postMessage - error getting lawyerIdx: " + ex.getMessage());
                    ex.printStackTrace();
                    return ResponseEntity.status(403).body(null);
                }
            }
            System.out.println("[DEBUG] ChatMessageController.postMessage - sender: LAWYER, id: " + senderId);
        }
        else { 
            System.out.println("[DEBUG] ChatMessageController.postMessage - no login user");
            return ResponseEntity.status(403).build(); 
        }

        if (!chatroomService.canPostMessage(roomId, senderType, senderId)) {
            System.out.println("[DEBUG] ChatMessageController.postMessage - canPostMessage returned false");
            return ResponseEntity.status(403).build();
        }
        System.out.println("[DEBUG] ChatMessageController.postMessage - canPostMessage passed");

        List<MultipartFile> fileList =
            (files == null) ? Collections.emptyList()
                            : Arrays.asList(files);

        try {
            System.out.println("[DEBUG] ChatMessageController.postMessage - calling chatDataService.sendMessage");
            ChatdataDTO saved = chatDataService.sendMessage(roomId, senderType, senderId, content, fileList);
            System.out.println("[DEBUG] ChatMessageController.postMessage - success, chatIdx: " + (saved != null ? saved.getChatIdx() : "null"));
            return ResponseEntity.ok(saved);
        } catch (IllegalStateException e) {
            System.out.println("[DEBUG] ChatMessageController.postMessage - IllegalStateException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(409).body(null);
        } catch (Exception e) {
            System.out.println("[DEBUG] ChatMessageController.postMessage - Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
