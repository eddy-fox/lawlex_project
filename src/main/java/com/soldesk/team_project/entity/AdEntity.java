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
@Table(name = "ad")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column(name = "ad_idx")
    private Integer adIdx;
    
    @Column(name = "ad_name")
    private String adName;

    @Column(name = "ad_imgPath")
    private String adImgPath;
    
    @Column(name = "ad_link")
    private String adLink;
    
    @Column(name = "ad_startDate")
    private LocalDate adStartDate;
    
    @Column(name = "ad_duration")
    private Integer adDuration;

    // @Column(name = "ad_cost")
    // private Integer adCost;

    @Column(name = "ad_views")
    private Integer adViews;

    @Column(name = "ad_active")
    private Integer adActive;

    @Column(name = "lawyer_idx", insertable = false, updatable = false)
    private Integer lawyerIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_idx")
    private LawyerEntity lawyer;

}
