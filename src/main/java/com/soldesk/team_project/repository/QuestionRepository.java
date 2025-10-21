package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.QuestionEntity;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Integer>{

    List<QuestionEntity> findByQAnswer(String qAnswer);

    List<QuestionEntity> findByQIdxAndQAnswer(Integer qIdx, String qAnswer);
    List<QuestionEntity> findByQTitleContainingIgnoreCaseAndQAnswerOrderByQTitleAsc(String QTitle, String qAnswer);
    List<QuestionEntity> findByQContentContainingIgnoreCaseAndQAnwerOrderByQContentAsc(String QContent, String qAnswer);
    List<QuestionEntity> findByMemberIdContainingIgnoreCaseAndQAnswerOrderByMemberIdAsc(String memberId, String qAnswer);

}