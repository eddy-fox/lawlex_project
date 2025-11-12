package com.soldesk.team_project.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    
    @Column(name = "reboard_content")
    private String reboardContent;

    @Column(name = "reboard_reg_date")
    private LocalDate reboardRegDate;

    @Column(name = "news_idx")
    private Integer newsIdx;

    @Column(name = "member_idx")
    private Integer memberIdx;

    @Column(name = "comment_active")
    private Integer commentActive;

    

}
