package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
