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

    @Column(name = "board_content")
    private String boardContent;

    @Column(name = "board_regDate")
    private LocalDate boardRegDate;

    @Column(name = "board_imgPath")
    private String boardImgPath;

    @Column(name = "board_caseDate")
    private LocalDate boardCaseDate;

    @Column(name = "board_views")
    private Integer boardViews;

    @JoinColumn(name = "member_idx")
    private MemberEntity memberIdx;

    @JoinColumn(name = "interest_idx")
    private InterestEntity intersetIdx;

    //작성자
    @ManyToOne
    private MemberEntity author;

    //수정시간
    private LocalDate modifyDate;

    @OneToMany(mappedBy = "boardEntity", cascade = CascadeType.REMOVE)
    private List<ReBoardEntity> reboardList;

}
