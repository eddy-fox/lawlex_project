package com.soldesk.team_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soldesk.team_project.entity.ReBoardEntity;

public interface ReBoardRepository extends JpaRepository<ReBoardEntity, Integer> {
    
    Optional<ReBoardEntity> findByBoardEntityBoardIdx(Integer boardIdx);

    // 변호사 idx 기준으로 최신 5개
    List<ReBoardEntity> findTop5ByLawyerIdxOrderByReboardRegDateDesc(Integer lawyerIdx);
    
}
