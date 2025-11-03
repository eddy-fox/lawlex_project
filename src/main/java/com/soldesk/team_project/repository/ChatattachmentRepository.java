package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.ChatAttachmentEntity;

@Repository
public interface ChatattachmentRepository extends JpaRepository<ChatAttachmentEntity, Integer> {
    List<ChatAttachmentEntity> findByChatDataChatIdxAndActiveOrderBySortOrderAscAttachmentIdAsc(Integer chatIdx, Integer active);
}
