package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soldesk.team_project.entity.LawyerEntity;

public interface RankingRepository extends JpaRepository<LawyerEntity, Integer> {
    
    List<LawyerEntity> findAllByOrderByLawyerLikeDesc(Pageable pageable);
    List<LawyerEntity> findAllByOrderByLawyerAnswerCntDesc(Pageable pageable);
}
