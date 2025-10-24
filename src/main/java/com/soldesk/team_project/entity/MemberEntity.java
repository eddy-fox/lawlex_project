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
@Table(name = "member")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column(name = "member_idx")
    private Integer memberIdx;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "member_pass")
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

    @Column(name = "member_active")
    private Integer memberActive;

    @Column(name = "interest_idx", insertable = false, updatable = false)
    private Integer interestIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_idx")
    private InterestEntity interest;


    @OneToMany(mappedBy = "member")
    private java.util.List<PurchaseEntity> purchase;

}
