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
    private final PurchaseService purchaseService;

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
        d.setLawyerImgPath(e.getLawyer() != null ? e.getLawyer().getLawyerImgPath() : null);

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
        // 먼저 회원-변호사 조합의 활성 방을 찾음
        ChatroomEntity existing = chatroomRepo
            .findTopByMemberMemberIdxAndLawyerLawyerIdxAndChatroomActiveOrderByChatroomIdxDesc(
                memberIdx, lawyerIdx, 1);

        if (existing != null) {
            String st = getOrDefault(existing.getState(), "PENDING");
            // PENDING 또는 ACTIVE 상태인 방은 재사용 (중복 방지)
            if ("PENDING".equalsIgnoreCase(st) || "ACTIVE".equalsIgnoreCase(st)) {
                return convertDTO(existing);
            }
            // 만료되었거나 종료된 채팅방이 있는 경우 재활성화
            // 기존 채팅방을 재사용하여 unique constraint 위반 방지
            existing.setChatroomName(member.getMemberName() + " - " + lawyer.getLawyerName());
            existing.setChatroomActive(1);
            existing.setMemberDeleted(0);
            existing.setLawyerDeleted(0);
            existing.setState("PENDING");
            existing.setRequestedAt(LocalDateTime.now());
            existing.setAcceptedAt(null);
            existing.setExpiresAt(null);
            existing.setDurationMinutes(durationMinutes);
            existing.setPointCost(pointCost);
            existing.setLastMessage(null);
            existing.setLastMessageAt(null);
            existing.setMemberReadAt(null);
            existing.setLawyerReadAt(null);
            
            chatroomRepo.save(existing);
            return convertDTO(existing);
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
        // 시연용: 제한시간 2분으로 설정
        int dur = 2;
        e.setExpiresAt(e.getAcceptedAt().plusMinutes(dur));
        e.setState("ACTIVE");

        chatroomRepo.save(e);

        // 포인트 차감 로직
        if (e.getMember() != null && e.getPointCost() != null && e.getPointCost() > 0) {
            Integer memberIdx = e.getMember().getMemberIdx();
            int pointCost = e.getPointCost();
            try {
                purchaseService.usePoint(memberIdx, pointCost);
                System.out.println("[DEBUG] Point deducted: memberIdx=" + memberIdx + ", amount=" + pointCost);
            } catch (IllegalStateException ex) {
                // 포인트 부족 시 예외 처리
                System.err.println("[ERROR] Point deduction failed: " + ex.getMessage());
                throw new IllegalStateException("포인트가 부족하여 상담을 수락할 수 없습니다.");
            }
        }
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

    @Transactional
    public void endChat(Integer roomId, String who) {
        ChatroomEntity e = chatroomRepo.findById(roomId).orElseThrow();
        String currentState = getOrDefault(e.getState(), "PENDING");
        
        // ACTIVE 상태인 경우에만 종료 가능
        if ("ACTIVE".equalsIgnoreCase(currentState)) {
            e.setState("EXPIRED");
            e.setChatroomActive(0);
            chatroomRepo.save(e);
        } else {
            throw new IllegalStateException("종료할 수 없는 상태입니다.");
        }
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
        if (e == null) {
            System.out.println("[DEBUG] canPostMessage - room not found: " + roomId);
            return false;
        }

        String state = getOrDefault(e.getState(), "PENDING");
        System.out.println("[DEBUG] canPostMessage - roomId: " + roomId + ", state: " + state + ", senderType: " + senderType + ", senderId: " + senderId);

        // ACTIVE 상태만 허용 (PENDING 상태에서는 메시지 전송 불가)
        if (!"ACTIVE".equalsIgnoreCase(state)) {
            System.out.println("[DEBUG] canPostMessage - invalid state (must be ACTIVE): " + state);
            return false;
        }

        // 만료 시간 체크
        if (e.getExpiresAt() != null && LocalDateTime.now().isAfter(e.getExpiresAt())) {
            System.out.println("[DEBUG] canPostMessage - room expired: " + e.getExpiresAt());
            return false;
        }

        boolean isMember = "MEMBER".equalsIgnoreCase(senderType)
                && e.getMember() != null
                && e.getMember().getMemberIdx().equals(senderId);
        boolean isLawyer = "LAWYER".equalsIgnoreCase(senderType)
                && e.getLawyer() != null
                && e.getLawyer().getLawyerIdx().equals(senderId);

        System.out.println("[DEBUG] canPostMessage - isMember: " + isMember + ", isLawyer: " + isLawyer);
        if (e.getMember() != null) {
            System.out.println("[DEBUG] canPostMessage - room memberIdx: " + e.getMember().getMemberIdx());
        }
        if (e.getLawyer() != null) {
            System.out.println("[DEBUG] canPostMessage - room lawyerIdx: " + e.getLawyer().getLawyerIdx());
        }

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
            .map(e -> {
                ChatRoomDTO dto = convertDTO(e);
                // 일반회원용: 변호사 이름만 표시 (예: "강대권변호사")
                if (e.getLawyer() != null && e.getLawyer().getLawyerName() != null) {
                    String lawyerName = e.getLawyer().getLawyerName();
                    dto.setChatroomName(lawyerName + "변호사");
                }
                // 읽지 않은 메시지 수 계산 (일반회원용: 변호사가 보낸 메시지)
                long unreadCount = chatdataRepo.countUnreadMessagesForMemberByRoom(e.getChatroomIdx());
                dto.setUnreadCount((int) unreadCount);
                return dto;
            })
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
            
            // chatroomActive = 1인 종료된 방 조회
            List<ChatroomEntity> endedListActive = chatroomRepo
                .findByMemberMemberIdxAndStateInAndChatroomActiveOrderByLastMessageAtDesc(
                    memberIdx, endedStates, 1, pr);
            
            // chatroomActive = 0인 종료된 방도 조회 (일찍 종료한 방 포함)
            List<ChatroomEntity> endedListInactive = chatroomRepo
                .findByMemberMemberIdxAndStateInAndChatroomActiveOrderByLastMessageAtDesc(
                    memberIdx, endedStates, 0, pr);
            
            System.out.println("[DEBUG] Found " + endedListActive.size() + " active ended rooms and " + endedListInactive.size() + " inactive ended rooms");
            
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
            
            // 종료된 방과 만료된 ACTIVE 방 합치기 (중복 제거)
            java.util.Set<Integer> roomIds = new java.util.HashSet<>();
            list = new java.util.ArrayList<>();
            for (ChatroomEntity e : endedListActive) {
                if (!roomIds.contains(e.getChatroomIdx())) {
                    list.add(e);
                    roomIds.add(e.getChatroomIdx());
                }
            }
            for (ChatroomEntity e : endedListInactive) {
                if (!roomIds.contains(e.getChatroomIdx())) {
                    list.add(e);
                    roomIds.add(e.getChatroomIdx());
                }
            }
            for (ChatroomEntity e : expiredActiveRooms) {
                if (!roomIds.contains(e.getChatroomIdx())) {
                    list.add(e);
                    roomIds.add(e.getChatroomIdx());
                }
            }
            System.out.println("[DEBUG] Total ended rooms: " + list.size() + " (active ended: " + endedListActive.size() + ", inactive ended: " + endedListInactive.size() + ", expired ACTIVE: " + expiredActiveRooms.size() + ")");
        } else {
            list = chatroomRepo
                .findByMemberMemberIdxAndStateAndChatroomActiveOrderByLastMessageAtDesc(
                    memberIdx, state.toUpperCase(), 1, pr);
        }
        
        // memberDeleted 필터링 및 정렬
        return list.stream()
            .filter(e -> e.getMemberDeleted() == null || e.getMemberDeleted() == 0)
            .map(e -> {
                ChatRoomDTO dto = convertDTO(e);
                // 일반회원용: 변호사 이름만 표시 (예: "강대권변호사")
                if (e.getLawyer() != null && e.getLawyer().getLawyerName() != null) {
                    String lawyerName = e.getLawyer().getLawyerName();
                    dto.setChatroomName(lawyerName + "변호사");
                }
                // 읽지 않은 메시지 수 계산 (일반회원용: 변호사가 보낸 메시지)
                long unreadCount = chatdataRepo.countUnreadMessagesForMemberByRoom(e.getChatroomIdx());
                dto.setUnreadCount((int) unreadCount);
                return dto;
            })
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
        // 일반회원: 읽지 않은 메시지 수만 (대기중 방 제외)
        long unread = chatdataRepo.countUnreadMessagesForMember(memberIdx);
        return Map.of("pending", 0, "active", 0, "unread", (int) unread);
    }

    /* ===================== 목록/배지: 변호사 ===================== */

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> findRoomsForLawyerByState(Integer lawyerIdx, String state, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        List<ChatroomEntity> list = chatroomRepo
            .findByLawyerLawyerIdxAndStateAndChatroomActiveOrderByLastMessageAtDesc(
                lawyerIdx, state.toUpperCase(), 1, pr);
        return list.stream()
            .map(e -> {
                ChatRoomDTO dto = convertDTO(e);
                // 변호사용: 일반회원 이름만 표시
                if (e.getMember() != null && e.getMember().getMemberName() != null) {
                    dto.setChatroomName(e.getMember().getMemberName());
                }
                // 읽지 않은 메시지 수 계산 (변호사용: 일반회원이 보낸 메시지)
                long unreadCount = chatdataRepo.countUnreadMessagesForLawyerByRoom(e.getChatroomIdx());
                dto.setUnreadCount((int) unreadCount);
                return dto;
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> findRoomsForLawyerByStates(Integer lawyerIdx, List<String> states, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        List<ChatroomEntity> list = chatroomRepo
            .findByLawyerLawyerIdxAndStateInAndChatroomActiveOrderByLastMessageAtDesc(
                lawyerIdx, states, 1, pr);
        return list.stream()
            .map(e -> {
                ChatRoomDTO dto = convertDTO(e);
                // 변호사용: 일반회원 이름만 표시
                if (e.getMember() != null && e.getMember().getMemberName() != null) {
                    dto.setChatroomName(e.getMember().getMemberName());
                }
                // 읽지 않은 메시지 수 계산 (변호사용: 일반회원이 보낸 메시지)
                long unreadCount = chatdataRepo.countUnreadMessagesForLawyerByRoom(e.getChatroomIdx());
                dto.setUnreadCount((int) unreadCount);
                return dto;
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLawyerRoomBadges(Integer lawyerIdx) {
        // 변호사: 대기중 방 수 + 읽지 않은 메시지 수
        int pending = chatroomRepo
            .countByLawyerLawyerIdxAndStateAndChatroomActive(lawyerIdx, "PENDING", 1);
        long unread = chatdataRepo.countUnreadMessagesForLawyer(lawyerIdx);
        System.out.println("[DEBUG] getLawyerRoomBadges - lawyerIdx: " + lawyerIdx + ", pending: " + pending);
        return Map.of("pending", pending, "active", 0, "unread", (int) unread);
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
