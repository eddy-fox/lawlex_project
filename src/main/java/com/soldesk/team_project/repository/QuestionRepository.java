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

    List<QuestionEntity> findByQuestionAnswerOrderByQuestionIdxDesc(String qAnswer);

    List<QuestionEntity> findByQuestionIdxAndQuestionAnswer(Integer qIdx, String qAnswer);
    List<QuestionEntity> findByQuestionTitleContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(String qTitle, String qAnswer);
    List<QuestionEntity> findByQuestionContentContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(String qContent, String qAnswer);
    List<QuestionEntity> findByMember_MemberIdContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(String memberId, String qAnswer);
    // List<QuestionEntity> findByMember_MemberIdContainingIgnoreCaseAndqAnswer(String memberId, String qAnswer);

    QuestionEntity findByQuestionTitle(String questionTitle);
    QuestionEntity findByQuestionTitleAndQuestionContent(String questionTitle, String questionContet);
    List<QuestionEntity> findByQuestionTitleLike(String questionTitle);
    Page<QuestionEntity> findAll(Pageable pageable);

    @Query("select "
            + "distinct q "
            + "from QuestionEntity q "
            + "left outer join MemberEntity u1 on q.author=u1 "
            + "left outer join AnswerEntity a on a.question=q "
            + "left outer join MemberEntity u2 on a.author=u2 "
            + "where "
            + "   q.questionTitle like %:kw% "
            + "   or q.questionContent like %:kw% "
            + "   or a.questionContent like %:kw% ")
    Page<QuestionEntity> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

}