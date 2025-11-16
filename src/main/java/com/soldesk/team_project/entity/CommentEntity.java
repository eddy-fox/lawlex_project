package com.soldesk.team_project.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_idx")
    private Integer commentIdx;
    
    @Column(name = "comment_content", columnDefinition = "TEXT")
    private String commentContent;

    @Column(name = "comment_regdate")
    private LocalDate commentRegDate;

    @Column(name = "news_idx")
    private Integer newsIdx;

    @Column(name = "comment_active")
    private Integer commentActive;

    // member_idx와 lawyer_idx를 직접 접근하기 위한 필드 (읽기 전용)
    @Column(name = "member_idx", insertable = false, updatable = false)
    private Integer memberIdx;
    
    @Column(name = "lawyer_idx", insertable = false, updatable = false)
    private Integer lawyerIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="lawyer_idx")
    private LawyerEntity lawyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_idx")
    private MemberEntity member;

    

}
