package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.CommentEntity;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {
    
    // 특정 뉴스 글의 활성 댓글 목록 조회 (member와 lawyer를 fetch join으로 함께 조회)
    @Query("SELECT DISTINCT c FROM CommentEntity c " +
           "LEFT JOIN FETCH c.member m " +
           "LEFT JOIN FETCH c.lawyer l " +
           "WHERE c.newsIdx = :newsIdx AND c.commentActive = :commentActive " +
           "ORDER BY c.commentRegDate DESC")
    List<CommentEntity> findByNewsIdxAndCommentActiveOrderByCommentRegDateDESC(
            @Param("newsIdx") Integer newsIdx, 
            @Param("commentActive") Integer commentActive);
    
    // 특정 뉴스 글의 모든 댓글 목록 조회 (관리자용, member와 lawyer를 fetch join으로 함께 조회)
    @Query("SELECT c FROM CommentEntity c " +
           "LEFT JOIN FETCH c.member m " +
           "LEFT JOIN FETCH c.lawyer l " +
           "WHERE c.newsIdx = :newsIdx " +
           "ORDER BY c.commentRegDate DESC")
    List<CommentEntity> findByNewsIdxOrderByCommentRegDateDESC(@Param("newsIdx") Integer newsIdx);
    
    // 댓글 단건 조회 (member와 lawyer를 fetch join으로 함께 조회)
    @Query("SELECT c FROM CommentEntity c " +
           "LEFT JOIN FETCH c.member m " +
           "LEFT JOIN FETCH c.lawyer l " +
           "WHERE c.commentIdx = :commentIdx")
    CommentEntity findByIdWithMemberAndLawyer(@Param("commentIdx") Integer commentIdx);
    
    // 로그인한 회원이 쓴 최근 5개 댓글 (활성 댓글만)
    List<CommentEntity> findTop5ByMemberIdxAndCommentActiveOrderByCommentRegDateDesc(
        Integer memberIdx,
        Integer commentActive
);
}
