package com.soldesk.team_project.entity;

import java.util.ArrayList;
import java.util.List;

import com.soldesk.team_project.security.UserBase;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Builder

public class MemberEntity implements UserBase{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column(name = "member_idx")
    private Integer memberIdx;

    @Column(name = "member_id", nullable = false, unique = true)
    private String memberId;

    @Column(name = "member_pass", nullable = false)
    private String memberPass;

    @Column(name = "member_name")
    private String memberName;

    @Column(name = "member_idnum")
    private String memberIdnum;

    @Column(name = "member_email")
    private String memberEmail;

    @Column(name = "member_phone")
    private String memberPhone;

    @Column(name = "member_agree")
    private String memberAgree;

    @Column(name = "member_nickname")
    private String memberNickname;

    @Column(name = "member_point", columnDefinition = "TINYINT(0) DEFAULT 0")
    private Integer memberPoint = 0;

    @Builder.Default
    @Column(name = "member_active", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Integer memberActive = 1;

    @Column(name = "provider")
    private String provider; // ex: "google"

    @Column(name = "provider_id")
    private String providerId; // ex: 구글 sub 값

    @Column(name = "interest_idx1")
    private Integer interestIdx1;

    @Column(name = "interest_idx2")
    private Integer interestIdx2;

    @Column(name = "interest_idx3")
    private Integer interestIdx3;

    @Column(name = "interest_idx")
    private Integer interestIdx;

    public void changePassword(String s){ this.memberPass = s; }
    public void changeNickname(String s){ this.memberNickname = s; }
    public void deactivate(){ this.memberActive = 0; }


    @OneToMany(mappedBy = "member")
    private List<PurchaseEntity> purchase;

    @OneToMany(mappedBy = "member")
    private List<PointEntity> point;

    @OneToMany(mappedBy = "member")
    private List<QuestionEntity> question;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberInterestEntity> memberInterests = new ArrayList<>();

    // Oauth2User용 메서드
    @Override
    public Integer getIdx() {
        return this.memberIdx;
    }

    @Override
    public String getEmail() {
        return this.memberEmail;
    }

    @Override
    public String getName() {
        return this.memberName;
    }

    @Override
    public Integer isActive() {
        return this.memberActive;
    }

}
