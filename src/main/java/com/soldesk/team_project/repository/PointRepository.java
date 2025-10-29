package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soldesk.team_project.entity.PointEntity;

public interface PointRepository extends JpaRepository<PointEntity, Integer>{
    
    // PointEntity findByMemberIdx(Integer memberIdx);
    // 포인트 사용 내역 조회
    List<PointEntity> findByMemberIdxOrderByPointIdxDesc(Integer memberIdx);

}
