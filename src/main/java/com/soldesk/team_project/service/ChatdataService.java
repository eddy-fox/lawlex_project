package com.soldesk.team_project.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.soldesk.team_project.dto.ChatAttachmentDTO;
import com.soldesk.team_project.dto.ChatRoomDTO;
import com.soldesk.team_project.dto.ChatdataDTO;
import com.soldesk.team_project.entity.ChatAttachmentEntity;
import com.soldesk.team_project.entity.ChatdataEntity;
import com.soldesk.team_project.infra.DriveUploader;
import com.soldesk.team_project.repository.ChatattachmentRepository;
import com.soldesk.team_project.repository.ChatdataRepository;
import com.soldesk.team_project.repository.ChatroomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatdataService {

    private final ChatdataRepository chatdataRepo;
    private final ChatattachmentRepository attachRepo;
    private final ChatroomRepository chatroomRepo;

    private final ChatroomService chatroomService; // 마지막 메시지 갱신/권한검사 재사용

    private final DriveUploader driveUploader; // (뉴스보드와 동일한 업로더 사용)

    @Value("${google.drive.chatting-folder-id}")
    private String chatFolderId;

    /* ===================== DTO 변환 ===================== */

    private ChatAttachmentDTO toDto(ChatAttachmentEntity a) {
    ChatAttachmentDTO d = new ChatAttachmentDTO();
    d.setAttachmentId(a.getAttachmentId()); // DTO 필드명 확인!
    d.setChatIdx(a.getChatData() != null ? a.getChatData().getChatIdx() : null);
    d.setFileUrl(a.getFileUrl());
    d.setFileName(a.getFileName());
    d.setContentType(a.getContentType());
    d.setFileSize(a.getFileSize());
    d.setSortOrder(a.getSortOrder());
    d.setActive(a.getActive());
    d.setCreatedAt(a.getCreatedAt());
    return d;
}

private ChatdataDTO toDto(ChatdataEntity e) {
    ChatdataDTO d = new ChatdataDTO();
    d.setChatIdx(e.getChatIdx());
    d.setChatContent(e.getChatContent());
    d.setChatRegDate(e.getChatRegDate());
    d.setSenderType(e.getSenderType());
    d.setSenderId(e.getSenderId());
    d.setChatActive(e.getChatActive());
    d.setChatroomIdx(e.getChatroom() != null ? e.getChatroom().getChatroomIdx() : null);

    if (e.getAttachments() != null && !e.getAttachments().isEmpty()) {
        List<ChatAttachmentDTO> list = e.getAttachments().stream()
            .filter(a -> a.getActive() == null || a.getActive() == 1) // null도 표시로 간주
            .sorted(
                java.util.Comparator
                    .comparing(ChatAttachmentEntity::getSortOrder, java.util.Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(ChatAttachmentEntity::getAttachmentId)
            )
            .map(this::toDto)
            .collect(java.util.stream.Collectors.toList());
        d.setAttachments(list);
    } else {
        // JDK 9+ : List.of();  JDK 8 : Collections.emptyList();
        d.setAttachments(java.util.Collections.emptyList());
    }
    return d;
}


    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    /* ===================== 메시지 전송 ===================== */

    /**
     * 텍스트/첨부 혼합 전송.
     * - 권한/만료/상태 검증은 chatroomService.canPostMessage(...)로 이중 확인.
     * - 저장 후 chatroomService.updateLastMessage(...)로 방의 마지막 메시지 갱신.
     */
    @Transactional
    public ChatdataDTO sendMessage(Integer roomId,
                                   String senderType,
                                   Integer senderId,
                                   String content,
                                   List<MultipartFile> files) throws Exception {

        // 1) 방/권한/만료/상태 체크
        if (!chatroomService.canPostMessage(roomId, senderType, senderId)) {
            throw new IllegalStateException("메시지를 보낼 수 없는 상태거나 권한이 없습니다.");
        }

        var room = chatroomRepo.findById(roomId).orElseThrow();

        // 2) ChatdataEntity 생성
        ChatdataEntity msg = new ChatdataEntity();
        msg.setChatroom(room);
        msg.setChatContent((content != null && !content.isBlank()) ? content : null);
        msg.setChatRegDate(LocalDateTime.now());
        msg.setSenderType(senderType.toUpperCase()); // "MEMBER"/"LAWYER"
        msg.setSenderId(senderId);
        msg.setChatActive(1);

        // 3) 먼저 메시지 저장(채번 필요)
        chatdataRepo.save(msg);

        // 4) 첨부 저장 (옵션)
        List<ChatAttachmentEntity> savedAttachments = new ArrayList<>();
        if (files != null) {
            int order = 0;
            for (MultipartFile f : files) {
                if (f == null || f.isEmpty()) continue;

                // 업로드 (DriveUploader 사용)
                var info = driveUploader.upload(f, chatFolderId);
                // info.name(), info.directUrl() 사용 (뉴스보드와 동일 패턴)

                ChatAttachmentEntity att = new ChatAttachmentEntity();
                att.setChatData(msg);
                att.setFileUrl(info.directUrl());
                att.setFileName(info.name());
                att.setContentType(f.getContentType());
                att.setFileSize((int) f.getSize()); // Entity가 Integer라 캐스팅
                att.setSortOrder(order++);
                att.setActive(1);
                att.setCreatedAt(LocalDateTime.now());

                attachRepo.save(att);
                savedAttachments.add(att);
            }
            // 양방향 컬렉션에 추가(선택)
            if (msg.getAttachments() != null) {
                msg.getAttachments().addAll(savedAttachments);
            }
        }

        // 5) 마지막 메시지 미리보기 구성
        String preview;
        if (content != null && !content.isBlank()) {
            preview = truncate(content, 200);
        } else if (!savedAttachments.isEmpty()) {
            if (savedAttachments.size() == 1) {
                preview = "[파일] " + truncate(savedAttachments.get(0).getFileName(), 180);
            } else {
                preview = "[파일 " + savedAttachments.size() + "개]";
            }
        } else {
            preview = "(빈 메시지)";
        }

        chatroomService.updateLastMessage(roomId, preview, msg.getChatRegDate());

        // 6) DTO 반환 (프론트가 바로 그리도록)
        return toDto(msg);
    }

    /* ===================== 히스토리 로딩(커서 방식) ===================== */

    /**
     * 최신 size개를 chat_idx 내림차순으로 가져온 뒤, 화면 표시를 위해 ASC로 뒤집어서 반환.
     */
    @Transactional(readOnly = true)
    public List<ChatdataDTO> loadLatestBatch(Integer roomId, int size) {
        var pr = PageRequest.of(0, size);
        var list = chatdataRepo
                .findByChatroomChatroomIdxAndChatActiveOrderByChatIdxDesc(roomId, 1, pr);
        // 최신→과거로 가져왔으므로 화면은 ASC가 자연스러워서 뒤집기
        Collections.reverse(list);
        return list.stream().map(this::toDto).toList();
    }

    /**
     * 스크롤 업: beforeId(현재 맨 위 메시지 chat_idx)보다 작은 것 중 최신 size개
     * 역시 DESC로 가져와서 ASC로 뒤집어 반환.
     */
    @Transactional(readOnly = true)
    public List<ChatdataDTO> loadHistoryBefore(Integer roomId, Integer beforeId, int size) {
        var pr = PageRequest.of(0, size);
        var list = chatdataRepo
                .findByChatroomChatroomIdxAndChatActiveAndChatIdxLessThanOrderByChatIdxDesc(
                        roomId, 1, beforeId, pr);
        Collections.reverse(list);
        return list.stream().map(this::toDto).toList();
    }

    /* ===================== 소프트 삭제 ===================== */

    /**
     * 작성자만 삭제 가능. 메시지/첨부 active=0 로 깃발만 내린다.
     * (방의 lastMessage는 굳이 과거로 롤백하지 않고, 다음 메시지 전송 시 갱신되는 전략을 권장)
     */
    @Transactional
    public boolean softDelete(Integer chatId, String senderType, Integer senderId) {
        var msg = chatdataRepo.findById(chatId).orElse(null);
        if (msg == null || msg.getChatActive() != null && msg.getChatActive() == 0) return false;

        // 작성자 검증
        boolean owner = senderType.equalsIgnoreCase(msg.getSenderType())
                && senderId != null && senderId.equals(msg.getSenderId());
        if (!owner) return false;

        msg.setChatActive(0);

        // 첨부들도 소프트 삭제
        if (msg.getAttachments() != null) {
            for (var a : msg.getAttachments()) {
                a.setActive(0);
                attachRepo.save(a);
            }
        }
        chatdataRepo.save(msg);
        return true;
    }
}
