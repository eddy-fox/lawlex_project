package com.soldesk.team_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soldesk.team_project.entity.ReBoardEntity;

public interface ReBoardRepository extends JpaRepository<ReBoardEntity, Integer> {
    
    Optional<ReBoardEntity> findByBoardEntityBoardIdx(Integer boardIdx);

    // 변호사 idx 기준으로 최신 5개
    List<ReBoardEntity> findTop5ByLawyerIdxOrderByReboardRegDateDesc(Integer lawyerIdx);
    
    // 변호사별 답변글 조회
    @Query("SELECT r FROM ReBoardEntity r WHERE r.lawyer.lawyerIdx = :lawyerIdx " +
           "AND (r.reboardActive = 1 OR r.reboardActive IS NULL) " +
           "ORDER BY r.reboardRegDate DESC")
    Page<ReBoardEntity> findByLawyerLawyerIdxOrderByReboardRegDateDesc(
        @Param("lawyerIdx") Integer lawyerIdx, 
        Pageable pageable);

}
