package com.soldesk.team_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.QuestionEntity;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Integer>{

    List<QuestionEntity> findByQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(Integer qAnswer, Integer qActive);

    List<QuestionEntity> findByQuestionIdxAndQuestionAnswerAndQuestionActive(Integer qIdx, Integer qAnswer, Integer qActive);
    List<QuestionEntity> findByQuestionTitleContainingIgnoreCaseAndQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(String qTitle, Integer qAnswer, Integer qActive);
    List<QuestionEntity> findByQuestionContentContainingIgnoreCaseAndQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(String qContent, Integer qAnswer, Integer qActive);
    List<QuestionEntity> findByMember_MemberIdContainingIgnoreCaseAndQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(String memberId, Integer qAnswer, Integer qActive);
    // List<QuestionEntity> findByMember_MemberIdContainingIgnoreCaseAndqAnswer(String memberId, String qAnswer);

    Page<QuestionEntity> findAllByOrderByQuestionRegDateDescQuestionIdxDesc(Pageable pageable); /* 모두 조회 */
    Page<QuestionEntity> findByMemberIdxOrderByQuestionRegDateDescQuestionIdxDesc(Integer mIdx, Pageable pageable); /* 자신에 글 조회 */
    Page<QuestionEntity> findByLawyerIdxOrderByQuestionRegDateDescQuestionIdxDesc(Integer LIdx, Pageable pageable); /* 자신에 글 조회 */

    QuestionEntity findByAnswerAnswerIdx(Integer aIdx);
}