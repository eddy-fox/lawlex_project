package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.PurchaseEntity;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Integer>{

    // 결제 금액 조회를 위한 결제번호 조회
    PurchaseEntity findByPurchaseId(String purchaseId);

    // 구매 내역 조회
    List<PurchaseEntity> findByMemberIdxAndPurchaseStateOrderByPurchaseIdxDesc(int memberIdx, String purchaseState);
    
}
