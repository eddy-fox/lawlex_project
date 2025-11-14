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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "a_idx")
    private Integer answerIdx;

    @Column(name = "a_content")
    private String answerContent;

    @Column(name = "a_reg_date")
    private LocalDate answerRegDate;

    @Column(name = "a_active", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Integer answerActive = 1;

    @Column(name = "q_idx", insertable = false, updatable = false)
    private Integer questionIdx;

    @Column(name = "admin_idx", insertable = false, updatable = false)
    private Integer adminIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "q_idx")
    private QuestionEntity question;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_idx")
    private AdminEntity admin;

    // 날짜 자동 설정
    @PrePersist
    public void prePersist() {
        if (this.answerRegDate == null) {
            this.answerRegDate = LocalDate.now();
        }
    }
    
}
