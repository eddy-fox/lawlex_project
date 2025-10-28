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

    @Column(name = "reboard_content")
    private String reboardContent;

    @Column(name = "reboard_regDate")
    private LocalDate reboardRegDate;

    @JoinColumn(name = "board_idx")
    private BoardEntity boardIdx;

    @JoinColumn(name = "lawyer_idx")
    private LawyerEntity lawyerIdx;

    @ManyToOne
    private BoardEntity boardEntity;

    @ManyToOne
    private MemberEntity memberName;

    @ManyToOne
    private MemberEntity author;

    private LocalDate modifyDate;

}
