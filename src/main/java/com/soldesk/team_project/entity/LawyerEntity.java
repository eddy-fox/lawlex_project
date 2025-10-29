package com.soldesk.team_project.entity;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lawyer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawyerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "lawyer_idx")
    private Integer lawyerIdx;

    @Column(name = "lawyer_id")
    private String lawyerId;

    @Column(name = "lawyer_pass")
    private String lawyerPass;

    @Column(name = "lawyer_name")
    private String lawyerName;

    @Column(name = "lawyer_idnum")
    private String lawyerIdnum;

    @Column(name = "lawyer_email")
    private String lawyerEmail;

    @Column(name = "lawyer_phone")
    private String lawyerPhone;

    @Column(name = "lawyer_agree")
    private String lawyerAgree;

    @Column(name = "lawyer_nickname")
    private String lawyerNickname;

    @Column(name = "lawyer_auth")
    private Integer lawyerAuth;

    @Column(name = "lawyer_address")
    private String lawyerAddress;

    @Column(name = "lawyer_tel")
    private String lawyerTel;

    @Column(name = "lawyer_imgPath")
    private String lawyerImgPath;

    @Column(name = "lawyer_comment")
    private String lawyerComment;
    
    @Column(name = "lawyer_like")
    private Integer lawyerLike;
    
    @Column(name = "lawyer_answerCnt")
    private Integer lawyerAnswerCnt;

    @Column(name = "lawyer_active")
    private Integer lawyerActive;
    
    @Column(name = "interest_idx", insertable = false, updatable = false)
    private Integer interestIdx;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_idx")
    private InterestEntity interest;
    

}
