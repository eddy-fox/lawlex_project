package com.soldesk.team_project.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AnswerEntity {
    
    private LawyerEntity lawyerIdx;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDate answerRegDate;

    @ManyToOne
    private QuestionEntity question;

    @ManyToOne
    private MemberEntity author;

    private LocalDate modifyDate;

    @ManyToMany
    Set<MemberEntity> voter;
    
}
