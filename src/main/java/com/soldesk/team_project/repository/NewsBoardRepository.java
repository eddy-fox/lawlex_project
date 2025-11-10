package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.NewsBoardEntity;

@Repository
public interface NewsBoardRepository extends JpaRepository<NewsBoardEntity, Integer> {

    // 카테고리 + 활성글만
    Page<NewsBoardEntity> findByCategoryCategoryIdxAndNewsActiveOrderByNewsIdxDesc(
            Integer categoryIdx,
            int newsActive,
            Pageable pageable
    );
    // 전체 활성글 최신순
    List<NewsBoardEntity> findByNewsActiveOrderByNewsIdxDesc(int newsActive);

    // 카테고리별 갯수 (활성만 세고 싶으면 이것도 newsActive 조건 추가)
    int countByCategoryCategoryIdxAndNewsActive(int categoryIdx, int newsActive);
    

    List<NewsBoardEntity> findByCategoryCategoryIdxAndNewsActiveOrderByNewsIdxDesc(
        Integer categoryIdx,
        int newsActive
);
        
}
