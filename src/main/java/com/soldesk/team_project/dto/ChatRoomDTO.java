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

    private Integer memberIdx;
    private Integer lawyerIdx;

}
