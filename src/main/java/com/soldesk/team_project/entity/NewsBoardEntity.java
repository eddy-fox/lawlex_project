package com.soldesk.team_project.entity;

import java.util.Date;

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
@Table(name="news")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsBoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="news_idx")
    private Integer news_idx;
    
    @Column(name="news_title")
    private String news_title;

    @Column(name="news_content")
    private String news_content;

    @Column(name="news_regDate")
    private Date news_regDate;

    @Column(name="news_imgPath")
    private String news_imgPath;

    @Column(name="news_like")
    private Integer news_like;

    @Column(name="news_views")
    private Integer news_views;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="admin_idx")
    private AdminEntity admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="lawyer_idx")
    private LawyerEntity lawyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_idx")
    private NewsCategoryEntity category; 
    

}
