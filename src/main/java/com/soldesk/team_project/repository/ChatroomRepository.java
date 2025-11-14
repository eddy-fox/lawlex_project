package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.ChatroomEntity;

@Repository
public interface ChatroomRepository extends JpaRepository<ChatroomEntity, Integer> {

    boolean existsByChatroomIdxAndMember_MemberIdx(Integer roomId, Integer memberIdx);
    boolean existsByChatroomIdxAndLawyer_LawyerIdx(Integer roomId, Integer lawyerIdx);

    // 멤버-변호사 쌍의 활성 방(최근 것 1개)
    ChatroomEntity findTopByMemberMemberIdxAndLawyerLawyerIdxAndChatroomActiveOrderByChatroomIdxDesc(
            Integer memberIdx, Integer lawyerIdx, Integer chatroomActive);

    // [회원] 내 방 목록(상태 전체) - 최근 대화순
    List<ChatroomEntity> findByMemberMemberIdxAndChatroomActiveOrderByLastMessageAtDesc(
            Integer memberIdx, Integer chatroomActive, Pageable pageable);

    // [회원] 내 방 목록(특정 상태) - 최근 대화순
    List<ChatroomEntity> findByMemberMemberIdxAndStateAndChatroomActiveOrderByLastMessageAtDesc(
            Integer memberIdx, String state, Integer chatroomActive, Pageable pageable);

    // [변호사] 내 방 목록(특정 상태) - 최근 대화순
    List<ChatroomEntity> findByLawyerLawyerIdxAndStateAndChatroomActiveOrderByLastMessageAtDesc(
            Integer lawyerIdx, String state, Integer chatroomActive, Pageable pageable);

    // [변호사] 내 방 목록(여러 상태) - 최근 대화순
    List<ChatroomEntity> findByLawyerLawyerIdxAndStateInAndChatroomActiveOrderByLastMessageAtDesc(
            Integer lawyerIdx, List<String> states, Integer chatroomActive, Pageable pageable);

    // 배지 카운트(회원/변호사)
    int countByMemberMemberIdxAndStateAndChatroomActive(Integer memberIdx, String state, Integer chatroomActive);
    int countByLawyerLawyerIdxAndStateAndChatroomActive(Integer lawyerIdx, String state, Integer chatroomActive);

    // [회원] 내 방 목록(여러 상태) - 최근 대화순 (PENDING + ACTIVE 같이 조회용)
    List<ChatroomEntity> findByMemberMemberIdxAndStateInAndChatroomActiveOrderByLastMessageAtDesc(
            Integer memberIdx, List<String> states, Integer chatroomActive, Pageable pageable);

    // [회원] 내 방 목록(여러 상태, 삭제 안 한 것만) - 최근 대화순 (PENDING + ACTIVE 같이 조회용)
    // memberDeleted가 null이거나 0인 방만 조회
    @Query("SELECT c FROM ChatroomEntity c WHERE c.member.memberIdx = :memberIdx " +
           "AND c.state IN :states AND c.chatroomActive = :chatroomActive " +
           "AND (c.memberDeleted IS NULL OR c.memberDeleted = 0) " +
           "ORDER BY " +
           "CASE WHEN c.lastMessageAt IS NOT NULL THEN c.lastMessageAt " +
           "     WHEN c.acceptedAt IS NOT NULL THEN c.acceptedAt " +
           "     ELSE c.requestedAt END DESC")
    List<ChatroomEntity> findByMemberMemberIdxAndStateInAndChatroomActiveAndMemberDeletedOrderByLastMessageAtDesc(
            @Param("memberIdx") Integer memberIdx, 
            @Param("states") List<String> states, 
            @Param("chatroomActive") Integer chatroomActive, 
            Pageable pageable);
}
