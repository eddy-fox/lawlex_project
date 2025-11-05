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
@Table(name="news")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsBoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="news_idx")
    private Integer newsIdx;
    
    @Column(name="news_title")
    private String newsTitle;

    @Column(name="news_content")
    private String newsContent;

    @Column(name="news_regdate")
    private LocalDate newsRegDate;

    @Column(name="news_imgpath")
    private String newsImgPath;

    @Column(name="news_like")
    private Integer newsLike;

    @Column(name="news_views")
    private Integer newsViews;

    @Column(name="filename")
    private String storedFileName;

    @Column(name="fileattached")
    private int fileAttached;

    @Column(name="news_active")
    private int newsActive;
    
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
