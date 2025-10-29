package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

}