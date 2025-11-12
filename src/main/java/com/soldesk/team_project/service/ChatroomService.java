package com.soldesk.team_project.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.dto.ChatRoomDTO;
import com.soldesk.team_project.entity.ChatroomEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.repository.ChatdataRepository;
import com.soldesk.team_project.repository.ChatroomRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatroomService {

    private final ChatroomRepository chatroomRepo;
    private final ChatdataRepository chatdataRepo; // (옵션) 뱃지 계산 등에 사용 가능
    private final MemberRepository memberRepo;
    private final LawyerRepository lawyerRepo;

    /* ===================== DTO 변환 ===================== */

    private ChatRoomDTO convertDTO(ChatroomEntity e) {
        ChatRoomDTO d = new ChatRoomDTO();
        d.setChatroomIdx(e.getChatroomIdx());
        d.setChatroomName(e.getChatroomName());
        d.setChatroomActive(e.getChatroomActive());
        d.setMemberReadAt(e.getMemberReadAt());
        d.setLawyerReadAt(e.getLawyerReadAt());
        d.setMemberDeleted(e.getMemberDeleted());
        d.setLawyerDeleted(e.getLawyerDeleted());
        d.setLastMessageAt(e.getLastMessageAt());
        d.setLastMessage(e.getLastMessage());

        d.setState(getOrDefault(e.getState(), "PENDING"));
        d.setRequestedAt(e.getRequestedAt());
        d.setAcceptedAt(e.getAcceptedAt());
        d.setExpiresAt(e.getExpiresAt());
        d.setDurationMinutes(e.getDurationMinutes());
        d.setPointCost(e.getPointCost());

        d.setMemberIdx(e.getMember() != null ? e.getMember().getMemberIdx() : null);
        d.setLawyerIdx(e.getLawyer() != null ? e.getLawyer().getLawyerIdx() : null);

        d.setUnreadCount(null); // 필요 시 chatdataRepo로 계산해서 세팅
        return d;
    }

    private String getOrDefault(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    /* ===================== 단건 조회 ===================== */

    @Transactional(readOnly = true)
    public ChatRoomDTO getRoom(Integer roomId) {
        return chatroomRepo.findById(roomId).map(this::convertDTO).orElse(null);
    }

    /* ===================== 회원 → 변호사 상담 신청(PENDING) ===================== */

    @Transactional
    public ChatRoomDTO requestChat(Integer memberIdx, Integer lawyerIdx, int durationMinutes, int pointCost) {
        MemberEntity member = memberRepo.findById(memberIdx).orElse(null);
        LawyerEntity lawyer = lawyerRepo.findById(lawyerIdx).orElse(null);
        if (member == null || lawyer == null) throw new IllegalArgumentException("잘못된 회원/변호사 식별자");

        // 활성(PENDING/ACTIVE) 방이 이미 있으면 중복 생성 방지: 기존 방 반환
        ChatroomEntity existing = chatroomRepo
            .findTopByMemberMemberIdxAndLawyerLawyerIdxAndChatroomActiveOrderByChatroomIdxDesc(memberIdx, lawyerIdx, 1);
        if (existing != null) {
            String st = getOrDefault(existing.getState(), "PENDING");
            if ("PENDING".equalsIgnoreCase(st) || "ACTIVE".equalsIgnoreCase(st)) {
                return convertDTO(existing);
            }
        }

        // 신규 방 생성
        ChatroomEntity room = new ChatroomEntity();
        room.setChatroomName(member.getMemberName() + " - " + lawyer.getLawyerName()); // 필요 시 커스터마이즈
        room.setChatroomActive(1);
        room.setMemberDeleted(0);
        room.setLawyerDeleted(0);
        room.setState("PENDING");
        room.setRequestedAt(LocalDateTime.now());
        room.setDurationMinutes(durationMinutes);
        room.setPointCost(pointCost);
        room.setMember(member);
        room.setLawyer(lawyer);
        chatroomRepo.save(room);

        return convertDTO(room);
    }

    /* ===================== 변호사 수락/거절/비활성화 ===================== */

    @Transactional
    public void accept(Integer roomId, Integer lawyerIdx) {
        ChatroomEntity e = chatroomRepo.findById(roomId).orElseThrow();
        if (e.getLawyer() == null || !e.getLawyer().getLawyerIdx().equals(lawyerIdx)) {
            throw new SecurityException("해당 변호사의 방이 아닙니다.");
        }
        if (!"PENDING".equalsIgnoreCase(getOrDefault(e.getState(), "PENDING"))) {
            throw new IllegalStateException("대기중 상태가 아닙니다.");
        }

        e.setAcceptedAt(LocalDateTime.now());
        int dur = (e.getDurationMinutes() != null && e.getDurationMinutes() > 0) ? e.getDurationMinutes() : 60;
        e.setExpiresAt(e.getAcceptedAt().plusMinutes(dur));
        e.setState("ACTIVE");
        chatroomRepo.save(e);

       Integer memberIdx = e.getMember().getMemberIdx();
       int pointCost = e.getPointCost();   // 100 or 200
    // purchaseService.minusPoint(memberIdx, pointCost);  ← 이런 식으로
    }

    @Transactional
    public void decline(Integer roomId, Integer lawyerIdx) {
        ChatroomEntity e = chatroomRepo.findById(roomId).orElseThrow();
        if (e.getLawyer() == null || !e.getLawyer().getLawyerIdx().equals(lawyerIdx)) {
            throw new SecurityException("해당 변호사의 방이 아닙니다.");
        }
        if (!"PENDING".equalsIgnoreCase(getOrDefault(e.getState(), "PENDING"))) {
            throw new IllegalStateException("대기중 상태가 아닙니다.");
        }
        e.setState("DECLINED");
        chatroomRepo.save(e);
    }

    @Transactional
    public void deactivate(Integer roomId) {
        ChatroomEntity e = chatroomRepo.findById(roomId).orElseThrow();
        e.setChatroomActive(0);
        chatroomRepo.save(e);
    }

    /* ===================== 읽음 처리 ===================== */

    @Transactional
    public void touchReadAt(Integer roomId, String who) {
        ChatroomEntity e = chatroomRepo.findById(roomId).orElseThrow();
        if ("MEMBER".equalsIgnoreCase(who)) {
            e.setMemberReadAt(LocalDateTime.now());
        } else if ("LAWYER".equalsIgnoreCase(who)) {
            e.setLawyerReadAt(LocalDateTime.now());
        }
        chatroomRepo.save(e);
    }

    /* ===================== 메시지 관련 유틸 ===================== */

    @Transactional(readOnly = true)
    public boolean canPostMessage(Integer roomId, String senderType, Integer senderId) {
        ChatroomEntity e = chatroomRepo.findById(roomId).orElse(null);
        if (e == null) return false;

        if (!"ACTIVE".equalsIgnoreCase(getOrDefault(e.getState(), "PENDING"))) return false;
        if (e.getExpiresAt() != null && LocalDateTime.now().isAfter(e.getExpiresAt())) return false;

        boolean isMember = "MEMBER".equalsIgnoreCase(senderType)
                && e.getMember() != null && e.getMember().getMemberIdx().equals(senderId);
        boolean isLawyer = "LAWYER".equalsIgnoreCase(senderType)
                && e.getLawyer() != null && e.getLawyer().getLawyerIdx().equals(senderId);
        return isMember || isLawyer;
    }

    @Transactional
    public void updateLastMessage(Integer roomId, String preview, LocalDateTime at) {
        ChatroomEntity e = chatroomRepo.findById(roomId).orElseThrow();
        e.setLastMessage(truncate(preview, 200));
        e.setLastMessageAt(at != null ? at : LocalDateTime.now());
        chatroomRepo.save(e);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    /* ===================== 목록/배지: 회원 ===================== */

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> findRoomSummariesForMember(Integer memberIdx, String state, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        List<ChatroomEntity> list;
        if (state == null || state.isBlank()) {
            list = chatroomRepo.findByMemberMemberIdxAndChatroomActiveOrderByLastMessageAtDesc(memberIdx, 1, pr);
        } else {
            list = chatroomRepo.findByMemberMemberIdxAndStateAndChatroomActiveOrderByLastMessageAtDesc(
                    memberIdx, state.toUpperCase(), 1, pr);
        }
        return list.stream().map(this::convertDTO).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMemberRoomBadges(Integer memberIdx) {
        int pending = chatroomRepo.countByMemberMemberIdxAndStateAndChatroomActive(memberIdx, "PENDING", 1);
        int active  = chatroomRepo.countByMemberMemberIdxAndStateAndChatroomActive(memberIdx, "ACTIVE", 1);
        Integer unread = 0; // 필요 시 chatdataRepo로 계산
        return Map.of("pending", pending, "active", active, "unread", unread);
    }

    /* ===================== 목록/배지: 변호사 ===================== */

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> findRoomsForLawyerByState(Integer lawyerIdx, String state, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        List<ChatroomEntity> list = chatroomRepo
            .findByLawyerLawyerIdxAndStateAndChatroomActiveOrderByLastMessageAtDesc(lawyerIdx, state.toUpperCase(), 1, pr);
        return list.stream().map(this::convertDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> findRoomsForLawyerByStates(Integer lawyerIdx, List<String> states, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        List<ChatroomEntity> list = chatroomRepo
            .findByLawyerLawyerIdxAndStateInAndChatroomActiveOrderByLastMessageAtDesc(lawyerIdx, states, 1, pr);
        return list.stream().map(this::convertDTO).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLawyerRoomBadges(Integer lawyerIdx) {
        int pending = chatroomRepo.countByLawyerLawyerIdxAndStateAndChatroomActive(lawyerIdx, "PENDING", 1);
        int active  = chatroomRepo.countByLawyerLawyerIdxAndStateAndChatroomActive(lawyerIdx, "ACTIVE", 1);
        Integer unread = 0; // 필요 시 chatdataRepo로 계산
        return Map.of("pending", pending, "active", active, "unread", unread);
    }

    @Transactional(readOnly = true)
    public boolean isParticipant(Integer roomId, Integer memberIdx, Integer lawyerIdx) {
    var room = chatroomRepo.findById(roomId).orElse(null);
    if (room == null) return false;

    boolean memberOk = false;
    boolean lawyerOk = false;

    if (memberIdx != null && room.getMember() != null) {
        memberOk = memberIdx.equals(room.getMember().getMemberIdx());
    }
    if (lawyerIdx != null && room.getLawyer() != null) {
        lawyerOk = lawyerIdx.equals(room.getLawyer().getLawyerIdx());
    }
    return memberOk || lawyerOk;
}
}
