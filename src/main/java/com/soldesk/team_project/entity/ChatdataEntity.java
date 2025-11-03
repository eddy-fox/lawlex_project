package com.soldesk.team_project.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "chatdata",
    indexes = {
        @Index(name = "ix_chat_room_date", columnList = "chatroom_idx, chat_regDate DESC"),
        @Index(name = "ix_chat_sender", columnList = "sender_type, sender_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatdataEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_idx")
    private Integer chatIdx;

    @Column(name="chat_content")
    private String chatContent;

    @Column(name="chat_regDate")
    private LocalDateTime chatRegDate;

    @Column(name="sender_type")
    private String senderType;
    
    @Column(name="sender_id")
    private Integer senderId;

    @Column(name="chat_active")
    private Integer chatActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chatroom_idx")
    private ChatroomEntity chatroom;

    @OneToMany(mappedBy = "chatData", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, attachmentId ASC")
    private List<ChatAttachmentEntity> attachments = new ArrayList<>();


}
