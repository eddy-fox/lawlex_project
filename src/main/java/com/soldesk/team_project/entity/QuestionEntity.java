package com.soldesk.team_project.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    private String questionSecret;
    
    @Column(name = "q_answer")
    private String questionAnswer;

    @Column(name = "q_active")
    private Integer questionActive;

    @Column(name = "member_idx", insertable = false, updatable = false)
    private Integer memberIdx;

    @Column(name = "lawyer_idx", insertable = false, updatable = false)
    private Integer lawyerIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private MemberEntity member;

<<<<<<< HEAD
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_idx")
    private LawyerEntity lawyer;
=======
    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<AnswerEntity> answerList;
    
    @ManyToOne
    private MemberEntity author;

    private LocalDate modifyDate;

    @ManyToMany
    Set<MemberEntity> voter;
>>>>>>> main
    
}