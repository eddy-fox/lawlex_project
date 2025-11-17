package com.soldesk.team_project.dto;


import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomDTO {
    
    private Integer chatroomIdx;
    private String chatroomName;
    private Integer chatroomActive;
    private LocalDateTime memberReadAt;
    private LocalDateTime lawyerReadAt;
    private Integer memberDeleted;
    private Integer lawyerDeleted;
    private LocalDateTime lastMessageAt;
    private String lastMessage;   
    private String state;            // PENDING / ACTIVE / DECLINED / EXPIRED / CANCELLED
    private LocalDateTime requestedAt;      // 회원 신청 시각
    private LocalDateTime acceptedAt;       // 변호사 수락 시각
    private LocalDateTime expiresAt;        // 만료 시각(acceptedAt + durationMinutes)
    private Integer durationMinutes;  // 30 또는 60 (상품에 따라)
    private Integer pointCost;        // 수락 시 차감 포인트(로그용)
     private Integer unreadCount;      // 안 읽은 메시지 수


    private Integer memberIdx;
    private Integer lawyerIdx;
    private String lawyerImgPath;  // 변호사 프로필 사진 경로

}
