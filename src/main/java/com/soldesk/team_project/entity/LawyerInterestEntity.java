package com.soldesk.team_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "lawyer_interest")
@Data
@IdClass(LawyerInterestId.class)
public class LawyerInterestEntity {

    @Id
    @Column(name = "lawyer_idx")
    private Integer lawyerIdx;

    @Id
    @Column(name = "interest_idx")
    private Integer interestIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_idx", insertable = false, updatable = false)
    private LawyerEntity lawyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_idx", insertable = false, updatable = false)
    private InterestEntity interest;

    // ...
}

