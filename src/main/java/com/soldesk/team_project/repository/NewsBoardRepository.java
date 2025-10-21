package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.NewsBoardEntity;

@Repository
public interface NewsBoardRepository extends JpaRepository<NewsBoardEntity, Integer> {
    
    //카테고리별 게시글 조회
    List<NewsBoardEntity> findByCategoryCategoryIdxOrderByNewsIdxDesc(Integer categoryIdx);

    //최신글
    List<NewsBoardEntity> findAllByOrderByNewsIdxDesc();

    
    int countByCategoryCategoryIdx(int categoryIdx);

}
