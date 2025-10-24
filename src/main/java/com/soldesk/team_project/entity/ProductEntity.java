package com.soldesk.team_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "product_idx")
    private Integer productIdx;

    @Column(name = "product_content")
    private String productContent;
    
    @Column(name = "product_price")
    private String productPrice;
    
    @Column(name = "product_active")
    private Integer productActive;

    @OneToMany(mappedBy = "product")
    private java.util.List<PurchaseEntity> purchase;

}
