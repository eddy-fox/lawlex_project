package com.soldesk.team_project.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.soldesk.team_project.dto.ChatdataDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.service.ChatdataService;
import com.soldesk.team_project.service.ChatroomService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatroomService chatroomService; // 권한/상태 체크
    private final ChatdataService chatDataService; // 실제 저장

    /** 클라이언트: fetch('/chat/messages', { method:'POST', body: FormData(...) }) */
    @PostMapping("/messages")
    @ResponseBody
    public ResponseEntity<ChatdataDTO> postMessage(
            @RequestParam Integer roomId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile[] files,
            @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
            @SessionAttribute(value = "loginLawyer",  required = false) LawyerDTO  loginLawyer
    ) throws Exception {

        String senderType; Integer senderId;
        if (loginMember != null) { senderType = "MEMBER"; senderId = loginMember.getMemberIdx(); }
        else if (loginLawyer != null) { senderType = "LAWYER"; senderId = loginLawyer.getLawyerIdx(); }
        else { return ResponseEntity.status(403).build(); }

        if (!chatroomService.canPostMessage(roomId, senderType, senderId)) {
            return ResponseEntity.status(403).build();
        }

        List<MultipartFile> fileList =
            (files == null) ? Collections.emptyList()
                            : Arrays.asList(files);

       ChatdataDTO saved = chatDataService
            .sendMessage(roomId, senderType, senderId, content, fileList);

        return ResponseEntity.ok(saved);
    }
}
