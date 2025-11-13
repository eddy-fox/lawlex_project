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
@Table(name = "question")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "q_idx")
    private Integer questionIdx;

    @Column(name = "q_title")
    private String questionTitle;
    
    @Column(name = "q_content")
    private String questionContent;
    
    @Column(name = "q_regDate")
    private LocalDate questionRegDate;
    
    @Column(name = "q_secret")
    private Integer questionSecret;
    
    @Column(name = "q_answer")
    private Integer questionAnswer;

    @Column(name = "q_active")
    private Integer questionActive;

    @Column(name = "member_idx")
    private Integer memberIdx;

    @Column(name = "lawyer_idx")
    private Integer lawyerIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx", insertable = false, updatable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_idx", insertable = false, updatable = false)
    private LawyerEntity lawyer; 
    
    
}