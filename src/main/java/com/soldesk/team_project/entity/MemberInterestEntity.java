// 2) 실제 엔티티
package com.soldesk.team_project.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "member_interest")
@Data
@IdClass(MemberInterestId.class)
public class MemberInterestEntity {

    @Id
    @Column(name = "member_idx")
    private Integer memberIdx;

    @Id
    @Column(name = "interest_idx")
    private Integer interestIdx;

    // FK 매핑해두면 편함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx", insertable = false, updatable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_idx", insertable = false, updatable = false)
    private InterestEntity interest;

    // getter/setter ...
}
