package com.soldesk.team_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soldesk.team_project.entity.ReBoardEntity;

public interface ReBoardRepository extends JpaRepository<ReBoardEntity, Integer> {
    
}
