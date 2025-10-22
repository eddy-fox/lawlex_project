package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.QuestionEntity;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Integer>{

    List<QuestionEntity> findByQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(String qAnswer, Integer qActive);

    List<QuestionEntity> findByQuestionIdxAndQuestionAnswerAndQuestionActive(Integer qIdx, String qAnswer, Integer qActive);
    List<QuestionEntity> findByQuestionTitleContainingIgnoreCaseAndQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(String qTitle, String qAnswer, Integer qActive);
    List<QuestionEntity> findByQuestionContentContainingIgnoreCaseAndQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(String qContent, String qAnswer, Integer qActive);
    List<QuestionEntity> findByMember_MemberIdContainingIgnoreCaseAndQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(String memberId, String qAnswer, Integer qActive);

}