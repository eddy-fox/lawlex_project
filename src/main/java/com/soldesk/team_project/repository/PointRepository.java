package com.soldesk.team_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soldesk.team_project.entity.PointEntity;

public interface PointRepository extends JpaRepository<PointEntity, Integer>{
    
    PointEntity findByMemberIdx(Integer memberIdx);

}
