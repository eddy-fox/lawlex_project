package com.soldesk.team_project.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Table(
    name = "chat_attachment",
    indexes = {
        @Index(name = "ix_attach_chat",       columnList = "chat_idx"),
        @Index(name = "ix_attach_chat_sort",  columnList = "chat_idx, sort_order")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatAttatchmentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="attatchment_id")
    private Integer attatchmentId;

    @Column(name="file_url")
    private String fileUrl;// 파일 URL/S3 경로
    
    @Column(name="file_name")
    private String fileName;        // 원본/표시 이름
    
    @Column(name="content_type")
    private String contentType;     // image/png, application/pdf ...
    
    @Column(name="file_size")
    private Integer fileSize;          // 바이트 단위

    @Column(name="sort_order")
    private Integer sortOrder;      // 표시 순서(기본 0)
    
    @Column(name="active")
    private Integer active;         // 1=표시, 0=숨김(소프트삭제)

    @Column(name="created_at")
    private LocalDateTime createdAt; // 업로드 시각

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chat_idx")
    private Integer chatIdx;        // FK → chatdata.chat_idx
}
