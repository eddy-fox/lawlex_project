package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.QuestionEntity;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Integer>{

    List<QuestionEntity> findByQuestionAnswerOrderByQuestionIdxDesc(String qAnswer);

    List<QuestionEntity> findByQuestionIdxAndQuestionAnswer(Integer qIdx, String qAnswer);
    List<QuestionEntity> findByQuestionTitleContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(String qTitle, String qAnswer);
    List<QuestionEntity> findByQuestionContentContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(String qContent, String qAnswer);
    List<QuestionEntity> findByMember_MemberIdContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(String memberId, String qAnswer);

}