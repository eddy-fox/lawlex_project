package com.soldesk.team_project.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.ChatdataEntity;

@Repository
public interface ChatdataRepository extends JpaRepository<ChatdataEntity, Integer> {

    /** 최신 N개 로드 (DESC로 가져와서 서비스에서 reverse → ASC 표시) */
    List<ChatdataEntity> findByChatroomChatroomIdxAndChatActiveOrderByChatIdxDesc(
            Integer chatroomIdx, Integer chatActive, Pageable pageable);

    /** 커서: beforeId(현재 화면 맨 위 chat_idx)보다 작은 메시지들 중 최신 N개 (DESC) */
    List<ChatdataEntity> findByChatroomChatroomIdxAndChatActiveAndChatIdxLessThanOrderByChatIdxDesc(
            Integer chatroomIdx, Integer chatActive, Integer beforeChatIdx, Pageable pageable);

    /** 읽은 시간 이전의 마지막 메시지 찾기 */
    @Query("SELECT c FROM ChatdataEntity c WHERE c.chatroom.chatroomIdx = :roomId " +
           "AND c.chatActive = 1 AND c.chatRegDate <= :readAt " +
           "ORDER BY c.chatIdx DESC")
    List<ChatdataEntity> findLastReadMessages(
            @Param("roomId") Integer roomId,
            @Param("readAt") LocalDateTime readAt);

    /** 읽지 않은 메시지 수 카운트 (일반회원용: 변호사가 보낸 메시지 중 읽지 않은 것) */
    @Query("SELECT COUNT(c) FROM ChatdataEntity c WHERE c.chatroom.member.memberIdx = :memberIdx " +
           "AND c.chatActive = 1 AND c.senderType = 'LAWYER' " +
           "AND (c.chatroom.memberReadAt IS NULL OR c.chatRegDate > c.chatroom.memberReadAt) " +
           "AND c.chatroom.chatroomActive = 1 " +
           "AND c.chatroom.state = 'ACTIVE' " +
           "AND (c.chatroom.memberDeleted IS NULL OR c.chatroom.memberDeleted = 0)")
    long countUnreadMessagesForMember(@Param("memberIdx") Integer memberIdx);

    /** 읽지 않은 메시지 수 카운트 (변호사용: 일반회원이 보낸 메시지 중 읽지 않은 것) */
    @Query("SELECT COUNT(c) FROM ChatdataEntity c WHERE c.chatroom.lawyer.lawyerIdx = :lawyerIdx " +
           "AND c.chatActive = 1 AND c.senderType = 'MEMBER' " +
           "AND (c.chatroom.lawyerReadAt IS NULL OR c.chatRegDate > c.chatroom.lawyerReadAt) " +
           "AND c.chatroom.chatroomActive = 1 " +
           "AND (c.chatroom.lawyerDeleted IS NULL OR c.chatroom.lawyerDeleted = 0)")
    long countUnreadMessagesForLawyer(@Param("lawyerIdx") Integer lawyerIdx);

    /** 특정 채팅방의 읽지 않은 메시지 수 카운트 (일반회원용: 변호사가 보낸 메시지 중 읽지 않은 것) */
    @Query("SELECT COUNT(c) FROM ChatdataEntity c WHERE c.chatroom.chatroomIdx = :roomId " +
           "AND c.chatActive = 1 AND c.senderType = 'LAWYER' " +
           "AND (c.chatroom.memberReadAt IS NULL OR c.chatRegDate > c.chatroom.memberReadAt) " +
           "AND c.chatroom.chatroomActive = 1 " +
           "AND c.chatroom.state = 'ACTIVE' " +
           "AND (c.chatroom.memberDeleted IS NULL OR c.chatroom.memberDeleted = 0)")
    long countUnreadMessagesForMemberByRoom(@Param("roomId") Integer roomId);

    /** 특정 채팅방의 읽지 않은 메시지 수 카운트 (변호사용: 일반회원이 보낸 메시지 중 읽지 않은 것) */
    @Query("SELECT COUNT(c) FROM ChatdataEntity c WHERE c.chatroom.chatroomIdx = :roomId " +
           "AND c.chatActive = 1 AND c.senderType = 'MEMBER' " +
           "AND (c.chatroom.lawyerReadAt IS NULL OR c.chatRegDate > c.chatroom.lawyerReadAt) " +
           "AND c.chatroom.chatroomActive = 1 " +
           "AND (c.chatroom.lawyerDeleted IS NULL OR c.chatroom.lawyerDeleted = 0)")
    long countUnreadMessagesForLawyerByRoom(@Param("roomId") Integer roomId);
}