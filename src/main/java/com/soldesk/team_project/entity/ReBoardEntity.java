package com.soldesk.team_project.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reboard")
public class ReBoardEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reboard_idx")
    private Integer reboardIdx;

    @Column(name = "reboard_title")
    private String reboardTitle;

    @Column(name = "reboard_content", columnDefinition = "TEXT")
    private String reboardContent;

    @Column(name = "reboard_reg_date")
    private LocalDate reboardRegDate;

    @Column(name = "reboard_active")
    private Integer reboardActive;

    @Column(name = "lawyer_idx", insertable = false, updatable = false)
    private Integer lawyerIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_idx")
    private LawyerEntity lawyer;

    @Column(name = "board_idx", insertable = false, updatable = false)
    private Integer boardIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_idx")
    private BoardEntity boardEntity;


    @ManyToMany
    Set<LawyerEntity> voter;

    @ManyToMany
    Set<MemberEntity> memberVoter;

    @ManyToOne
    @JoinColumn(name = "member_idx")
    private MemberEntity memberIdx;

    private LocalDate modifyDate;

    // 날짜 null이면 자동 설정
    @PrePersist
    public void prePersist() {
        if (this.reboardRegDate == null) {
            this.reboardRegDate = LocalDate.now();
        }
    }

}
