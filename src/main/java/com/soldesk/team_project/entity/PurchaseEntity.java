package com.soldesk.team_project.entity;

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
@Table(name = "purchase")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "purchase_idx", insertable = false, updatable = false)
    private Integer purchaseIdx;
    
    @Column(name = "product_idx", insertable = false, updatable = false)
    private Integer productIdx;

    @Column(name = "member_idx", insertable = false, updatable = false)
    private Integer memberIdx;

    @Column(name = "lawyer_idx", insertable = false, updatable = false)
    private Integer lawyerIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_idx")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_idx")
    private LawyerEntity lawyer;
    
}
