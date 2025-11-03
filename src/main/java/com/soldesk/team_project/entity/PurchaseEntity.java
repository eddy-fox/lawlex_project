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

    @Column(name = "purchase_idx")
    private Integer purchaseIdx;
    
    @Column(name = "product_idx", insertable = false, updatable = false)
    private Integer productIdx;

    @Column(name = "member_idx", insertable = false, updatable = false)
    private Integer memberIdx;
    
    @Column(name = "purchase_id")
    private String purchaseId;

    @Column(name = "purchase_state")
    private String purchaseState;

    @Column(name = "purchase_legDate")
    private LocalDate purchaseLegDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_idx")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx")
    private MemberEntity member;
    
}
