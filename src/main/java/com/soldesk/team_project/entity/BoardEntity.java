package com.soldesk.team_project.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class BoardEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer board_idx;

    @Column(length = 200)
    private String board_title;

    @Column(columnDefinition = "TEXT")
    private String board_content;

    private LocalDate board_regDate;

    private String board_imgPath;

    private LocalDate board_caseDate;

    private Integer board_views;

    @JoinColumn(name = "member_idx")
    private MemberEntity member_idx;

    @JoinColumn(name = "interest_idx")
    private InterestEntity interset_idx;


    //작성자
    @ManyToOne
    private MemberEntity author;

    //수정시간
    private LocalDate modifyDate;

    @OneToMany(mappedBy = "boardEntity", cascade = CascadeType.REMOVE)
    private List<ReBoardEntity> reboardList;

}
