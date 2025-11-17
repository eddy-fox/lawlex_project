package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT q FROM QuestionEntity q WHERE (q.questionActive = 1 OR q.questionActive IS NULL) ORDER BY q.questionRegDate DESC, q.questionIdx DESC")
    Page<QuestionEntity> findAllByOrderByQuestionRegDateDescQuestionIdxDesc(Pageable pageable); /* 모두 조회 */
    
    @Query("SELECT q FROM QuestionEntity q WHERE q.memberIdx = :mIdx AND (q.questionActive = 1 OR q.questionActive IS NULL) ORDER BY q.questionRegDate DESC, q.questionIdx DESC")
    Page<QuestionEntity> findByMemberIdxOrderByQuestionRegDateDescQuestionIdxDesc(@Param("mIdx") Integer mIdx, Pageable pageable); /* 자신에 글 조회 */
    
    @Query("SELECT q FROM QuestionEntity q WHERE q.lawyerIdx = :lIdx AND (q.questionActive = 1 OR q.questionActive IS NULL) ORDER BY q.questionRegDate DESC, q.questionIdx DESC")
    Page<QuestionEntity> findByLawyerIdxOrderByQuestionRegDateDescQuestionIdxDesc(@Param("lIdx") Integer lIdx, Pageable pageable); /* 자신에 글 조회 */

    QuestionEntity findByAnswerAnswerIdx(Integer aIdx);

    @Query("SELECT q FROM QuestionEntity q WHERE (q.questionActive = 1 OR q.questionActive IS NULL) AND (q.questionTitle LIKE %:qTitle% OR q.questionContent LIKE %:qContent%) ORDER BY q.questionRegDate DESC, q.questionIdx DESC")
    Page<QuestionEntity> findByQuestionTitleContainingOrQuestionContentContainingOrderByQuestionRegDateDescQuestionIdxDesc(@Param("qTitle") String qTitle, @Param("qContent") String qContent, Pageable pageable);

    @Query("""
                SELECT q
                FROM QuestionEntity q
                WHERE q.memberIdx = :mIdx
                AND (q.questionActive = 1 OR q.questionActive IS NULL)
                AND ( q.questionTitle   LIKE %:keyword%   OR q.questionContent LIKE %:keyword% )
                ORDER BY q.questionRegDate DESC, q.questionIdx DESC
                                                                    """)
    Page<QuestionEntity> searchMemberQuestions(@Param("mIdx") Integer mIdx, @Param("keyword") String keyword, Pageable pageable);

    @Query("""
                SELECT q
                FROM QuestionEntity q
                WHERE q.lawyerIdx = :lIdx
                AND (q.questionActive = 1 OR q.questionActive IS NULL)
                AND ( q.questionTitle   LIKE %:keyword%   OR q.questionContent LIKE %:keyword% )
                ORDER BY q.questionRegDate DESC, q.questionIdx DESC
                                                                    """)
    Page<QuestionEntity> searchLawyerQuestions(@Param("lIdx") Integer lIdx, @Param("keyword") String keyword, Pageable pageable);
}
