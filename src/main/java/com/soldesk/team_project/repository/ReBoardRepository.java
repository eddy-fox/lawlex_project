package com.soldesk.team_project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soldesk.team_project.entity.ReBoardEntity;

public interface ReBoardRepository extends JpaRepository<ReBoardEntity, Integer> {
    
    Optional<ReBoardEntity> findByBoardBoardIdx(Integer boardIdx);

    @Query("SELECT r FROM ReBoardEntity r JOIN FETCH r.lawyer WHERE r.id = :id")
    Optional<ReBoardEntity> findByIdWithLawyer(@Param("id") Integer id);

    ReBoardEntity findByReboardIdx(Integer reboardIdx);

}
