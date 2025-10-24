package com.soldesk.team_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.PurchaseEntity;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Integer>{

    PurchaseEntity findByPurchaseId(String purchaseId);
    
}
