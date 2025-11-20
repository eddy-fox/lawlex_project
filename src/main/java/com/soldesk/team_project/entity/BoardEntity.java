package com.soldesk.team_project.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "board")
public class BoardEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_idx")
    private Integer boardIdx;

    @Column(name = "board_title")
    private String boardTitle;

    @Column(name = "board_content", columnDefinition = "TEXT")
    private String boardContent;

    @Column(name = "board_reg_date")
    private LocalDate boardRegDate;

    @Column(name = "board_img_path")
    private String boardImgPath;

    @Column(name = "board_case_date")
    private LocalDate boardCaseDate;

    @Column(name = "board_views")
    private Integer boardViews;

    @Column(name = "board_active")
    private Integer boardActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private MemberEntity member;

    @Column(name = "board_category")
    private String boardCategory;

    @Column(name = "board_imgid")
    private String boardImgid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_idx")
    private InterestEntity interest;

    @OneToMany(mappedBy = "boardEntity", cascade = CascadeType.REMOVE)
    private List<ReBoardEntity> reboardList;

}
