package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.ProductEntity;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer>{

    // 포인트 구매 상품 가져오기
    List<ProductEntity> findByProductContentContainingAndProductActiveOrderByProductIdxAsc(String productContent, Integer productActive);

    // 특정 상품 조회
    ProductEntity findByProductIdxAndProductActive(Integer productIdx, Integer productActive);

} 
