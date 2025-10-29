package com.soldesk.team_project.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.soldesk.team_project.dto.ChatdataDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.service.ChatdataService;
import com.soldesk.team_project.service.ChatroomService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatroomService chatroomService; // 참여자/상태 검증
    private final ChatdataService chatDataService; // 실제 저장

    /** 클라이언트: stomp.send('/app/chat.send', {}, JSON.stringify({roomId, content})) */
    @MessageMapping("/chat.send")
    public void send(@Payload ChatInbound in,
                     @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
                     @SessionAttribute(value = "loginLawyer",  required = false) LawyerDTO  loginLawyer) throws Exception {
        if (in == null || in.getRoomId() == null) return;

        String senderType; Integer senderId;
        if (loginMember != null) { senderType = "MEMBER"; senderId = loginMember.getMemberIdx(); }
        else if (loginLawyer != null) { senderType = "LAWYER"; senderId = loginLawyer.getLawyerIdx(); }
        else { return; }

        // 방 상태/만료/권한 체크
        if (!chatroomService.canPostMessage(in.getRoomId(), senderType, senderId)) return;

        // 저장 (텍스트만)
        ChatdataDTO saved = chatDataService.sendMessage(in.getRoomId(), senderType, senderId, in.getContent(), null);

        // 구독 채널로 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/" + in.getRoomId(), saved);
    }

    public static class ChatInbound {
        private Integer roomId;
        private String content;
        public Integer getRoomId() { return roomId; }
        public void setRoomId(Integer roomId) { this.roomId = roomId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
