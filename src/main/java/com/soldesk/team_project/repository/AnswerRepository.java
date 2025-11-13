package com.soldesk.team_project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.AnswerEntity;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, Integer>{
    
    AnswerEntity findByQuestionIdxAndAnswerActive(int qIdx, int aActive);

}
