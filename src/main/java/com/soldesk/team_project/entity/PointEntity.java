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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "point_idx")
    private Integer pointIdx;

    @Column(name = "point_division")
    private String pointDivision;

    @Column(name = "point_state")
    private Integer pointState;

    @Column(name = "point_history")
    private String pointHistory;

    @Column(name = "point_reg_date")
    private LocalDate pointRegDate;

    @Column(name = "member_idx", insertable = false, updatable = false)
    private Integer memberIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private MemberEntity member;

    // 날짜 null이면 자동 설정
    @PrePersist
    public void prePersist() {
        if (this.pointRegDate == null) {
            this.pointRegDate = LocalDate.now();
        }
    }

}
