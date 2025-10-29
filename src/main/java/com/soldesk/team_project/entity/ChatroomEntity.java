package com.soldesk.team_project.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
    name = "chatroom",
    indexes = {
            @Index(name = "ix_room_member_active", columnList = "member_idx, chatroom_active"),
            @Index(name = "ix_room_lawyer_active", columnList = "lawyer_idx, chatroom_active"),
            @Index(name = "ix_room_last_at", columnList = "last_message_at")
        }
    )
@Getter 
@Setter
@ToString(exclude = {"member","lawyer"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatroom_idx")
    private Integer chatroomIdx;

    @Column(name="chatroom_name")
    private String chatroomName;

    @Column(name="chatroom_active", nullable = false)
    private Integer chatroomActive;

    @Column(name="member_read_at")
    private LocalDateTime memberReadAt;

    @Column(name="lawyer_read_at")
    private LocalDateTime lawyerReadAt;

    @Column(name="member_deleted")
    private Integer memberDeleted;

    @Column(name="lawyer_deleted")
    private Integer lawyerDeleted;

    @Column(name="last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name="last_message", length = 200)
    private String lastMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_idx")
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="lawyer_idx")
    private LawyerEntity lawyer;
}
