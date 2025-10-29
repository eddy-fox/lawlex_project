package com.soldesk.team_project.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatAttachmentDTO {
    
    private Integer attatchmentId;
    private Integer chatIdx;        // FK → chatdata.chat_idx

    private String fileUrl;         // 파일 URL/S3 경로
    private String fileName;        // 원본/표시 이름
    private String contentType;     // image/png, application/pdf ...
    private Integer fileSize;          // 바이트 단위

    private Integer sortOrder;      // 표시 순서(기본 0)
    private Integer active;         // 1=표시, 0=숨김(소프트삭제)

    private LocalDateTime createdAt; // 업로드 시각
}
