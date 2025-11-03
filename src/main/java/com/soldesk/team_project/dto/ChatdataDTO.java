package com.soldesk.team_project.dto;


import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatdataDTO {
    
    private Integer chatIdx;
    private String chatContent;
    private LocalDateTime chatRegDate;
    private String senderType;
    private Integer senderId;
    private Integer chatActive;

    private Integer chatroomIdx;
    private List<ChatAttachmentDTO> attachments;
}
