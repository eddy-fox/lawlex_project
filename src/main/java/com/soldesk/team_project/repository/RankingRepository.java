package com.soldesk.team_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soldesk.team_project.entity.LawyerEntity;

public interface RankingRepository extends JpaRepository<LawyerEntity, Integer> {
    
}
