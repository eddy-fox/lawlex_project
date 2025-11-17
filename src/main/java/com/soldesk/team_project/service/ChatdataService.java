package com.soldesk.team_project.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.soldesk.team_project.dto.ChatAttachmentDTO;
import com.soldesk.team_project.dto.ChatdataDTO;
import com.soldesk.team_project.entity.ChatAttachmentEntity;
import com.soldesk.team_project.entity.ChatdataEntity;
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

    private final FirebaseStorageService storageService;

    /* ===================== DTO 변환 ===================== */

    private ChatAttachmentDTO toDto(ChatAttachmentEntity a) {
    ChatAttachmentDTO d = new ChatAttachmentDTO();
    d.setAttachmentId(a.getAttachmentId()); // DTO 필드명 확인!
    try {
        d.setChatIdx(a.getChatData() != null ? a.getChatData().getChatIdx() : null);
    } catch (Exception ex) {
        System.out.println("[DEBUG] ChatdataService.toDto(ChatAttachment) - error getting chatData: " + ex.getMessage());
        d.setChatIdx(null);
    }
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
    System.out.println("[DEBUG] ChatdataService.toDto - start, chatIdx: " + e.getChatIdx());
    ChatdataDTO d = new ChatdataDTO();
    d.setChatIdx(e.getChatIdx());
    d.setChatContent(e.getChatContent());
    d.setChatRegDate(e.getChatRegDate());
    d.setSenderType(e.getSenderType());
    d.setSenderId(e.getSenderId());
    d.setChatActive(e.getChatActive());
    try {
        d.setChatroomIdx(e.getChatroom() != null ? e.getChatroom().getChatroomIdx() : null);
    } catch (Exception ex) {
        System.out.println("[DEBUG] ChatdataService.toDto - error getting chatroom: " + ex.getMessage());
        ex.printStackTrace();
        d.setChatroomIdx(null);
    }

    try {
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
    } catch (Exception ex) {
        System.out.println("[DEBUG] ChatdataService.toDto - error getting attachments: " + ex.getMessage());
        ex.printStackTrace();
        d.setAttachments(java.util.Collections.emptyList());
    }
    System.out.println("[DEBUG] ChatdataService.toDto - completed, chatIdx: " + d.getChatIdx());
    return d;
}


    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private String nowUuidName(String originalFilename) {
    String ext = getExt(originalFilename);
    String now = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
    String shortUuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    return now + "-" + shortUuid + ext; // 예: 20251113_213015123-7f3a9c1b.jpg
}
private String getExt(String original) {
    if (original == null || original.isBlank()) return ".bin";
    int i = original.lastIndexOf('.');
    if (i < 0 || i == original.length() - 1) return ".bin";
    String ext = original.substring(i).toLowerCase(java.util.Locale.ROOT);
    if (ext.length() > 10 || ext.contains("/") || ext.contains("\\") || ext.contains(" ")) return ".bin";
    return ext;
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
        System.out.println("[DEBUG] ChatdataService.sendMessage - start, roomId: " + roomId + ", senderType: " + senderType + ", senderId: " + senderId);

        // 1) 방/권한/만료/상태 체크
        if (!chatroomService.canPostMessage(roomId, senderType, senderId)) {
            System.out.println("[DEBUG] ChatdataService.sendMessage - canPostMessage returned false");
            throw new IllegalStateException("메시지를 보낼 수 없는 상태거나 권한이 없습니다.");
        }
        System.out.println("[DEBUG] ChatdataService.sendMessage - canPostMessage passed");

        var room = chatroomRepo.findById(roomId).orElseThrow();
        System.out.println("[DEBUG] ChatdataService.sendMessage - room found, state: " + room.getState());

        // 2) ChatdataEntity 생성
        ChatdataEntity msg = new ChatdataEntity();
        msg.setChatroom(room);
        msg.setChatContent((content != null && !content.isBlank()) ? content : null);
        msg.setChatRegDate(LocalDateTime.now());
        msg.setSenderType(senderType.toUpperCase()); // "MEMBER"/"LAWYER"
        msg.setSenderId(senderId);
        msg.setChatActive(1);

        // 3) 먼저 메시지 저장(채번 필요)
        chatdataRepo.saveAndFlush(msg);
        System.out.println("[DEBUG] ChatdataService.sendMessage - saved msg, chatIdx: " + msg.getChatIdx());
        
        // 저장 후 다시 조회하여 모든 연관 관계를 로드
        ChatdataEntity savedMsg = chatdataRepo.findById(msg.getChatIdx()).orElseThrow();
        System.out.println("[DEBUG] ChatdataService.sendMessage - reloaded msg, chatIdx: " + savedMsg.getChatIdx());

        // 4) 첨부 저장 (옵션)
        List<ChatAttachmentEntity> savedAttachments = new ArrayList<>();
        if (files != null && files.size() > 0) {
            System.out.println("[DEBUG] ChatdataService.sendMessage - processing " + files.size() + " files");
            try {
                int order = 0;
                for (MultipartFile f : files) {
                    if (f == null || f.isEmpty()) {
                        System.out.println("[DEBUG] ChatdataService.sendMessage - skipping empty file");
                        continue;
                    }

                    System.out.println("[DEBUG] ChatdataService.sendMessage - uploading file: " + f.getOriginalFilename());
                    // ✅ Firebase 업로드
                    String filename = nowUuidName(f.getOriginalFilename());
                    // 방별로 하위 폴더 분리 (원하면 roomId 빼고 "chatdata/"만 써도 됩니다)
                    String objectPath = "chatdata/" + roomId + "/" + filename;

                    var uploaded = storageService.upload(f, objectPath); // id & url 동시 생성
                    System.out.println("[DEBUG] ChatdataService.sendMessage - file uploaded: " + uploaded.url());

                    ChatAttachmentEntity att = new ChatAttachmentEntity();
                    att.setChatData(savedMsg);
                    att.setFileUrl(uploaded.url());        // ✅ 화면에서 바로 쓰는 전체 URL
                    att.setFileName(filename);             // 예: 2025...-abcd1234.jpg
                    att.setContentType(f.getContentType());
                    att.setFileSize((int) f.getSize());    // Entity가 Integer면 캐스팅
                    att.setSortOrder(order++);
                    att.setActive(1);
                    att.setCreatedAt(LocalDateTime.now());

                    attachRepo.save(att);
                    savedAttachments.add(att);
                    System.out.println("[DEBUG] ChatdataService.sendMessage - attachment saved, id: " + att.getAttachmentId());
                }
            } catch (Exception ex) {
                System.out.println("[DEBUG] ChatdataService.sendMessage - error processing files: " + ex.getMessage());
                ex.printStackTrace();
                throw ex; // 파일 업로드 실패 시 예외 전파
            }
            // savedMsg를 다시 조회하면 attachments가 자동으로 로드됨
        } else {
            System.out.println("[DEBUG] ChatdataService.sendMessage - no files to process");
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

        chatroomService.updateLastMessage(roomId, preview, savedMsg.getChatRegDate());
        System.out.println("[DEBUG] ChatdataService.sendMessage - updated lastMessage");

        // 6) 첨부 파일 저장 후 다시 조회하여 attachments 포함
        if (!savedAttachments.isEmpty()) {
            savedMsg = chatdataRepo.findById(savedMsg.getChatIdx()).orElseThrow();
            System.out.println("[DEBUG] ChatdataService.sendMessage - reloaded msg with attachments, chatIdx: " + savedMsg.getChatIdx());
        }

        // 7) DTO 반환 (프론트가 바로 그리도록)
        // 저장 후 다시 조회한 엔티티를 사용하여 Lazy Loading 문제 방지
        System.out.println("[DEBUG] ChatdataService.sendMessage - calling toDto");
        ChatdataDTO result = toDto(savedMsg);
        System.out.println("[DEBUG] ChatdataService.sendMessage - toDto completed, chatIdx: " + result.getChatIdx());
        return result;
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

    /**
     * 읽은 시간 이전의 마지막 메시지의 chatIdx 반환
     * (---여기까지 읽었습니다--- 마커 표시용)
     */
    @Transactional(readOnly = true)
    public Integer findLastReadMessageChatIdx(Integer roomId, LocalDateTime readAt) {
        System.out.println("[DEBUG] ChatdataService.findLastReadMessageChatIdx - roomId: " + roomId + ", readAt: " + readAt);
        var messages = chatdataRepo.findLastReadMessages(roomId, readAt);
        System.out.println("[DEBUG] ChatdataService.findLastReadMessageChatIdx - found " + messages.size() + " messages");
        if (!messages.isEmpty()) {
            Integer lastReadIdx = messages.get(0).getChatIdx();
            System.out.println("[DEBUG] ChatdataService.findLastReadMessageChatIdx - lastReadChatIdx: " + lastReadIdx);
            return lastReadIdx;
        }
        System.out.println("[DEBUG] ChatdataService.findLastReadMessageChatIdx - no messages found");
        return null;
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
