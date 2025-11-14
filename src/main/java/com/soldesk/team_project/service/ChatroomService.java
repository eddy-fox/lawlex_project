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

        // lastMessageAt 이 null일 수도 있으니, 요청/수락 시각으로 대체
        LocalDateTime lmAt = e.getLastMessageAt();
        if (lmAt == null) {
            lmAt = (e.getAcceptedAt() != null) ? e.getAcceptedAt() : e.getRequestedAt();
        }
        d.setLastMessageAt(lmAt);
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

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    /* ===================== 단건 조회 ===================== */

    @Transactional(readOnly = true)
    public ChatRoomDTO getRoom(Integer roomId) {
        return chatroomRepo.findById(roomId).map(this::convertDTO).orElse(null);
    }

    /* ===================== 회원 → 변호사 상담 신청(PENDING) ===================== */

    /**
     * 회원이 변호사에게 상담 신청.
     * - 이미 같은 (member, lawyer) 조합으로 PENDING/ACTIVE & chatroomActive=1 인 방이 있으면
     *   → 새로 만들지 않고 그 방 DTO 그대로 반환
     * - 없으면 새 방 생성(PENDING)
     */
    @Transactional
    public ChatRoomDTO requestChat(Integer memberIdx, Integer lawyerIdx, int durationMinutes, int pointCost) {
        MemberEntity member = memberRepo.findById(memberIdx).orElse(null);
        LawyerEntity lawyer = lawyerRepo.findById(lawyerIdx).orElse(null);
        if (member == null || lawyer == null) {
            throw new IllegalArgumentException("잘못된 회원/변호사 식별자");
        }

        // 기존 활성 방(PENDING / ACTIVE) 재사용
        ChatroomEntity existing = chatroomRepo
            .findTopByMemberMemberIdxAndLawyerLawyerIdxAndChatroomActiveOrderByChatroomIdxDesc(
                memberIdx, lawyerIdx, 1);

        if (existing != null) {
            String st = getOrDefault(existing.getState(), "PENDING");
            if ("PENDING".equalsIgnoreCase(st) || "ACTIVE".equalsIgnoreCase(st)) {
                return convertDTO(existing);
            }
        }

        // 신규 방 생성
        ChatroomEntity room = new ChatroomEntity();
        room.setChatroomName(member.getMemberName() + " - " + lawyer.getLawyerName());
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
        int dur = (e.getDurationMinutes() != null && e.getDurationMinutes() > 0)
                ? e.getDurationMinutes()
                : 60;
        e.setExpiresAt(e.getAcceptedAt().plusMinutes(dur));
        e.setState("ACTIVE");

        chatroomRepo.save(e);

        // 포인트 차감 로직이 있다면 여기서:
        // Integer memberIdx = e.getMember().getMemberIdx();
        // int pointCost = e.getPointCost();
        // purchaseService.minusPoint(memberIdx, pointCost);
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

        // ACTIVE 상태 && 시간 만료 전
        if (!"ACTIVE".equalsIgnoreCase(getOrDefault(e.getState(), "PENDING"))) return false;
        if (e.getExpiresAt() != null && LocalDateTime.now().isAfter(e.getExpiresAt())) return false;

        boolean isMember = "MEMBER".equalsIgnoreCase(senderType)
                && e.getMember() != null
                && e.getMember().getMemberIdx().equals(senderId);
        boolean isLawyer = "LAWYER".equalsIgnoreCase(senderType)
                && e.getLawyer() != null
                && e.getLawyer().getLawyerIdx().equals(senderId);

        return isMember || isLawyer;
    }

    @Transactional
    public void updateLastMessage(Integer roomId, String preview, LocalDateTime at) {
        ChatroomEntity e = chatroomRepo.findById(roomId).orElseThrow();
        e.setLastMessage(truncate(preview, 200));
        e.setLastMessageAt(at != null ? at : LocalDateTime.now());
        chatroomRepo.save(e);
    }

    /* ===================== 회원 진행중 방 조회 (PENDING + ACTIVE) ===================== */

    /**
     * 헤더 채팅 아이콘용:
     * - 회원이 가진 PENDING / ACTIVE 상태의 방 전체 (최근 대화순)
     * - 회원이 삭제하지 않은 방만 (memberDeleted = 0 또는 null)
     * - 만료된 ACTIVE 방은 자동으로 EXPIRED로 변경
     */
    @Transactional
    public List<ChatRoomDTO> findMemberOngoingRooms(Integer memberIdx, int page, int size) {
        // 충분히 많이 가져와서 필터링 후 정렬
        PageRequest pr = PageRequest.of(0, 1000);
        List<String> states = List.of("PENDING", "ACTIVE");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("[DEBUG] findMemberOngoingRooms - memberIdx: " + memberIdx + ", states: " + states);

        // 기존 메서드 사용 (memberDeleted 필터 없이)
        List<ChatroomEntity> list = chatroomRepo
            .findByMemberMemberIdxAndStateInAndChatroomActiveOrderByLastMessageAtDesc(
                memberIdx, states, 1, pr);

        System.out.println("[DEBUG] Repository returned: " + list.size() + " entities");

        // 만료된 ACTIVE 방을 EXPIRED로 변경
        List<ChatroomEntity> expiredRooms = new java.util.ArrayList<>();
        for (ChatroomEntity e : list) {
            if ("ACTIVE".equalsIgnoreCase(getOrDefault(e.getState(), "")) 
                && e.getExpiresAt() != null 
                && now.isAfter(e.getExpiresAt())) {
                e.setState("EXPIRED");
                expiredRooms.add(e);
                System.out.println("[DEBUG] Room " + e.getChatroomIdx() + " expired at " + e.getExpiresAt());
            }
        }
        if (!expiredRooms.isEmpty()) {
            chatroomRepo.saveAll(expiredRooms);
            System.out.println("[DEBUG] Updated " + expiredRooms.size() + " expired rooms to EXPIRED");
            // list의 상태도 업데이트 (이미 변경된 엔티티이므로 자동 반영됨)
        }

        // Service 레벨에서 memberDeleted 필터링 및 정렬 개선
        // 만료된 방은 EXPIRED 상태로 변경되었으므로 결과에서 제외 (ONGOING은 PENDING+ACTIVE만)
        List<ChatRoomDTO> result = list.stream()
            .filter(e -> {
                // 만료되어 EXPIRED로 변경된 방은 제외 (ONGOING 조회이므로)
                String currentState = getOrDefault(e.getState(), "");
                if ("EXPIRED".equalsIgnoreCase(currentState)) {
                    System.out.println("[DEBUG] Filtering out expired room: " + e.getChatroomIdx());
                    return false;
                }
                boolean keep = e.getMemberDeleted() == null || e.getMemberDeleted() == 0;
                if (!keep) {
                    System.out.println("[DEBUG] Filtered out room: " + e.getChatroomIdx() + " (memberDeleted: " + e.getMemberDeleted() + ")");
                }
                return keep;
            })
            .map(this::convertDTO)
            .sorted((a, b) -> {
                // lastMessageAt이 있으면 그것으로, 없으면 acceptedAt 또는 requestedAt으로 정렬
                LocalDateTime aTime = a.getLastMessageAt() != null ? a.getLastMessageAt() 
                    : (a.getAcceptedAt() != null ? a.getAcceptedAt() : a.getRequestedAt());
                LocalDateTime bTime = b.getLastMessageAt() != null ? b.getLastMessageAt() 
                    : (b.getAcceptedAt() != null ? b.getAcceptedAt() : b.getRequestedAt());
                
                if (aTime == null && bTime == null) return 0;
                if (aTime == null) return 1;
                if (bTime == null) return -1;
                return bTime.compareTo(aTime); // 내림차순
            })
            .skip(page * size)
            .limit(size)
            .toList();

        System.out.println("[DEBUG] Final result: " + result.size() + " rooms");
        result.forEach(r -> System.out.println("[DEBUG] Room: " + r.getChatroomIdx() + ", state: " + r.getState() + ", name: " + r.getChatroomName()));

        return result;
    }

    /**
     * 메인/카드 화면에서:
     * - 이 회원이 이 변호사와 이미 PENDING 또는 ACTIVE 방이 있는지 확인
     * - 있으면 그 방 DTO 리턴, 없으면 null
     */
    @Transactional(readOnly = true)
    public ChatRoomDTO findMemberOngoingRoomWithLawyer(Integer memberIdx, Integer lawyerIdx) {
        ChatroomEntity existing = chatroomRepo
            .findTopByMemberMemberIdxAndLawyerLawyerIdxAndChatroomActiveOrderByChatroomIdxDesc(
                memberIdx, lawyerIdx, 1);

        if (existing == null) return null;

        String st = getOrDefault(existing.getState(), "PENDING");
        if ("PENDING".equalsIgnoreCase(st) || "ACTIVE".equalsIgnoreCase(st)) {
            return convertDTO(existing);
        }
        return null;
    }

    /* ===================== 목록/배지: 회원 ===================== */

    /**
     * /chat/api/member/rooms
     * state 가 null/빈문자 → 전체 상태
     * state 가 "PENDING"/"ACTIVE"/"EXPIRED" 같은 값이면 해당 상태만
     * EXPIRED 조회 시 만료된 ACTIVE 방도 포함
     */
    @Transactional
    public List<ChatRoomDTO> findRoomSummariesForMember(Integer memberIdx, String state, int page, int size) {
        PageRequest pr = PageRequest.of(0, 1000); // 충분히 많이 가져와서 필터링
        List<ChatroomEntity> list;
        LocalDateTime now = LocalDateTime.now();

        if (state == null || state.isBlank()) {
            list = chatroomRepo
                .findByMemberMemberIdxAndChatroomActiveOrderByLastMessageAtDesc(memberIdx, 1, pr);
        } else if ("EXPIRED".equalsIgnoreCase(state) || "ENDED".equalsIgnoreCase(state)) {
            // 종료된 방 조회: EXPIRED, DECLINED, CANCELLED 상태 + 만료된 ACTIVE 방
            List<String> endedStates = List.of("EXPIRED", "DECLINED", "CANCELLED");
            List<ChatroomEntity> endedList = chatroomRepo
                .findByMemberMemberIdxAndStateInAndChatroomActiveOrderByLastMessageAtDesc(
                    memberIdx, endedStates, 1, pr);
            
            System.out.println("[DEBUG] Found " + endedList.size() + " rooms with ended states: " + endedStates);
            
            // 만료된 ACTIVE 방도 찾기
            List<ChatroomEntity> activeList = chatroomRepo
                .findByMemberMemberIdxAndStateAndChatroomActiveOrderByLastMessageAtDesc(
                    memberIdx, "ACTIVE", 1, pr);
            
            System.out.println("[DEBUG] Found " + activeList.size() + " ACTIVE rooms to check for expiration");
            
            // 만료된 ACTIVE 방을 EXPIRED로 변경
            List<ChatroomEntity> expiredActiveRooms = new java.util.ArrayList<>();
            for (ChatroomEntity e : activeList) {
                if (e.getExpiresAt() != null && now.isAfter(e.getExpiresAt())) {
                    System.out.println("[DEBUG] Room " + e.getChatroomIdx() + " is expired (expiresAt: " + e.getExpiresAt() + ", now: " + now + ")");
                    e.setState("EXPIRED");
                    expiredActiveRooms.add(e);
                }
            }
            if (!expiredActiveRooms.isEmpty()) {
                chatroomRepo.saveAll(expiredActiveRooms);
                System.out.println("[DEBUG] Updated " + expiredActiveRooms.size() + " expired ACTIVE rooms to EXPIRED");
            }
            
            // 종료된 방과 만료된 ACTIVE 방 합치기
            list = new java.util.ArrayList<>(endedList);
            list.addAll(expiredActiveRooms);
            System.out.println("[DEBUG] Total ended rooms: " + list.size() + " (ended states: " + endedList.size() + ", expired ACTIVE: " + expiredActiveRooms.size() + ")");
        } else {
            list = chatroomRepo
                .findByMemberMemberIdxAndStateAndChatroomActiveOrderByLastMessageAtDesc(
                    memberIdx, state.toUpperCase(), 1, pr);
        }
        
        // memberDeleted 필터링 및 정렬
        return list.stream()
            .filter(e -> e.getMemberDeleted() == null || e.getMemberDeleted() == 0)
            .map(this::convertDTO)
            .sorted((a, b) -> {
                LocalDateTime aTime = a.getLastMessageAt() != null ? a.getLastMessageAt() 
                    : (a.getAcceptedAt() != null ? a.getAcceptedAt() : a.getRequestedAt());
                LocalDateTime bTime = b.getLastMessageAt() != null ? b.getLastMessageAt() 
                    : (b.getAcceptedAt() != null ? b.getAcceptedAt() : b.getRequestedAt());
                
                if (aTime == null && bTime == null) return 0;
                if (aTime == null) return 1;
                if (bTime == null) return -1;
                return bTime.compareTo(aTime); // 내림차순
            })
            .skip(page * size)
            .limit(size)
            .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMemberRoomBadges(Integer memberIdx) {
        int pending = chatroomRepo
            .countByMemberMemberIdxAndStateAndChatroomActive(memberIdx, "PENDING", 1);
        int active  = chatroomRepo
            .countByMemberMemberIdxAndStateAndChatroomActive(memberIdx, "ACTIVE", 1);

        Integer unread = 0; // 필요 시 chatdataRepo로 계산
        return Map.of("pending", pending, "active", active, "unread", unread);
    }

    /* ===================== 목록/배지: 변호사 ===================== */

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> findRoomsForLawyerByState(Integer lawyerIdx, String state, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        List<ChatroomEntity> list = chatroomRepo
            .findByLawyerLawyerIdxAndStateAndChatroomActiveOrderByLastMessageAtDesc(
                lawyerIdx, state.toUpperCase(), 1, pr);
        return list.stream().map(this::convertDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> findRoomsForLawyerByStates(Integer lawyerIdx, List<String> states, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        List<ChatroomEntity> list = chatroomRepo
            .findByLawyerLawyerIdxAndStateInAndChatroomActiveOrderByLastMessageAtDesc(
                lawyerIdx, states, 1, pr);
        return list.stream().map(this::convertDTO).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLawyerRoomBadges(Integer lawyerIdx) {
        int pending = chatroomRepo
            .countByLawyerLawyerIdxAndStateAndChatroomActive(lawyerIdx, "PENDING", 1);
        int active  = chatroomRepo
            .countByLawyerLawyerIdxAndStateAndChatroomActive(lawyerIdx, "ACTIVE", 1);

        Integer unread = 0; // 필요 시 chatdataRepo로 계산
        return Map.of("pending", pending, "active", active, "unread", unread);
    }

    /* ===================== 참여자 체크 ===================== */

    @Transactional(readOnly = true)
    public boolean isParticipant(Integer roomId, Integer memberIdx, Integer lawyerIdx) {
        ChatroomEntity room = chatroomRepo.findById(roomId).orElse(null);
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
