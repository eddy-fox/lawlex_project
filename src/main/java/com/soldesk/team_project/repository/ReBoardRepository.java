package com.soldesk.team_project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soldesk.team_project.entity.ReBoardEntity;

public interface ReBoardRepository extends JpaRepository<ReBoardEntity, Integer> {
    
    Optional<ReBoardEntity> findByBoardEntityBoardIdx(Integer boardIdx);

}
