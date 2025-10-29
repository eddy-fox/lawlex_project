package com.soldesk.team_project.dto;


import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatdataDTO {
    
    private Integer chatIdx;
    private String chatContent;
    private String chatFile;
    private LocalDateTime chatRefDate;
    private String senderType;
    private Integer senderId;
    private Integer chatActive;

    private Integer chatroomIdx;
}
