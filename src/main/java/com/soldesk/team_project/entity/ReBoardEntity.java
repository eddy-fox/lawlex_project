package com.soldesk.team_project.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ReBoardEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reboard_idx;

    @Column(length = 200)
    private String reboard_title;

    @Column(columnDefinition = "TEXT")
    private String reboard_content;

    private LocalDate reboard_regDate;

    @JoinColumn(name = "board_idx")
    private BoardEntity board_idx;

    @JoinColumn(name = "lawyer_idx")
    private LawyerEntity lawyer_idx;

    @ManyToOne
    private BoardEntity board;

    @ManyToOne
    private MemberEntity memberName;

    @ManyToOne
    private MemberEntity author;

    private LocalDate modifyDate;

    @ManyToMany
    Set<MemberEntity> voter;

}
