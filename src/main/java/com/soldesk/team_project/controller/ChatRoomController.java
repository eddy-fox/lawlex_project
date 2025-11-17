package com.soldesk.team_project.controller;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.soldesk.team_project.dto.ChatRoomDTO;
import com.soldesk.team_project.dto.ChatdataDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.service.ChatroomService;
import com.soldesk.team_project.service.ChatdataService;
import com.soldesk.team_project.service.CalendarService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatroomService chatroomService;
    private final ChatdataService chatDataService;
    private final CalendarService calendarService;
    private final LawyerRepository lawyerRepo;
    private final MemberRepository memberRepo;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/")
    public String chatGate(
            @SessionAttribute(value = "loginMember", required = false) Object loginMember,
            @SessionAttribute(value = "loginLawyer", required = false) Object loginLawyer
    ) {
        if (loginLawyer != null) {
            return "redirect:/chat/lawyer";
        } else if (loginMember != null) {
            return "redirect:/chat/member";
        } else {
            return "redirect:/member/login?need=로그인 후 이용이 가능합니다.";
        }
    }

    /* ====== 세션에서 memberIdx / lawyerIdx 뽑는 유틸 ====== */
    private Integer extractMemberId(Object sessionObj) {
        if (sessionObj == null) return null;
        if (sessionObj instanceof MemberDTO m) return m.getMemberIdx();
        try {
            Method m = sessionObj.getClass().getMethod("getMemberIdx");
            Object v = m.invoke(sessionObj);
            if (v instanceof Integer) return (Integer) v;
        } catch (Exception ignore) {}
        try {
            var f = sessionObj.getClass().getDeclaredField("memberIdx");
            f.setAccessible(true);
            Object v = f.get(sessionObj);
            if (v instanceof Integer) return (Integer) v;
        } catch (Exception ignore) {}
        return null;
    }

    private Integer extractLawyerId(Object sessionObj) {
        if (sessionObj == null) return null;
        if (sessionObj instanceof LawyerDTO l) return l.getLawyerIdx();
        try {
            Method m = sessionObj.getClass().getMethod("getLawyerIdx");
            Object v = m.invoke(sessionObj);
            if (v instanceof Integer) return (Integer) v;
        } catch (Exception ignore) {}
        try {
            var f = sessionObj.getClass().getDeclaredField("lawyerIdx");
            f.setAccessible(true);
            Object v = f.get(sessionObj);
            if (v instanceof Integer) return (Integer) v;
        } catch (Exception ignore) {}
        return null;
    }

    /* ===================== 공통: 방 입장/읽음/비활성 ===================== */

    @GetMapping("/room")
public String room(@RequestParam("roomId") Integer roomId,
                   @RequestParam(name = "size", defaultValue = "50") int size,
                   Model model,
                   HttpServletResponse resp,
                   @SessionAttribute(value = "loginMember", required = false) Object loginMember,
                   @SessionAttribute(value = "loginLawyer", required = false) Object loginLawyer
) throws Exception {
    ChatRoomDTO room = chatroomService.getRoom(roomId);
    if (room == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 채팅방");
        return null;
    }

    // 세션에서 id 추출
    Integer mId = extractMemberId(loginMember);
    Integer lId = extractLawyerId(loginLawyer);

    // 로그인 보강: 둘 다 null이면 403
    if (mId == null && lId == null) {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "로그인 필요");
        return null;
    }

    // PENDING 상태일 때: 일반회원은 접근 불가, 변호사만 접근 가능
    if ("PENDING".equalsIgnoreCase(room.getState())) {
        if (mId != null && lId == null) {
            // 일반회원이 PENDING 상태의 채팅방에 접근 시도
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "변호사의 수락을 기다려주세요.");
            return null;
        }
    }

    // ACTIVE 또는 EXPIRED 상태가 아니면 변호사만 접근 가능
    // 일반회원은 ACTIVE(진행중) 또는 EXPIRED(종료된 상담 내역 보기) 상태만 접근 가능
    String roomState = (room.getState() != null && !room.getState().isBlank()) ? room.getState() : "";
    if (!"ACTIVE".equalsIgnoreCase(roomState) && !"EXPIRED".equalsIgnoreCase(roomState) && mId != null && lId == null) {
        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "상담이 시작되지 않았습니다.");
        return null;
    }

    List<ChatdataDTO> initBatch = chatDataService.loadLatestBatch(roomId, size);
    model.addAttribute("room", room);
    model.addAttribute("initBatch", initBatch);

    // 템플릿에서 바로 쓰기 쉽게 모델에 심어줌
    model.addAttribute("meMemberIdx", mId);   // 회원이면 값, 아니면 null
    model.addAttribute("meLawyerIdx", lId);   // 변호사면 값, 아니면 null

    // 마지막 읽은 메시지 위치 찾기 (---여기까지 읽었습니다--- 마커용)
    Integer lastReadChatIdx = null;
    System.out.println("[DEBUG] ChatRoomController.room - mId: " + mId + ", lId: " + lId);
    System.out.println("[DEBUG] ChatRoomController.room - memberReadAt: " + room.getMemberReadAt() + ", lawyerReadAt: " + room.getLawyerReadAt());
    if (mId != null && room.getMemberReadAt() != null) {
        lastReadChatIdx = chatDataService.findLastReadMessageChatIdx(roomId, room.getMemberReadAt());
        System.out.println("[DEBUG] ChatRoomController.room - lastReadChatIdx (member): " + lastReadChatIdx);
    } else if (lId != null && room.getLawyerReadAt() != null) {
        lastReadChatIdx = chatDataService.findLastReadMessageChatIdx(roomId, room.getLawyerReadAt());
        System.out.println("[DEBUG] ChatRoomController.room - lastReadChatIdx (lawyer): " + lastReadChatIdx);
    }
    model.addAttribute("lastReadChatIdx", lastReadChatIdx);
    System.out.println("[DEBUG] ChatRoomController.room - final lastReadChatIdx: " + lastReadChatIdx);

    // 일반회원 화면용: 변호사 정보 조회
    if (mId != null && room.getLawyerIdx() != null) {
        lawyerRepo.findById(room.getLawyerIdx()).ifPresent(lawyerEntity -> {
            LawyerDTO lawyer = new LawyerDTO();
            lawyer.setLawyerIdx(lawyerEntity.getLawyerIdx());
            lawyer.setLawyerName(lawyerEntity.getLawyerName());
            lawyer.setLawyerImgPath(lawyerEntity.getLawyerImgPath());
            lawyer.setLawyerComment(lawyerEntity.getLawyerComment());
            model.addAttribute("lawyer", lawyer);
        });
    }

    // 변호사 화면용: 회원 정보 조회
    if (lId != null && room.getMemberIdx() != null) {
        memberRepo.findById(room.getMemberIdx()).ifPresent(memberEntity -> {
            MemberDTO member = new MemberDTO();
            member.setMemberIdx(memberEntity.getMemberIdx());
            member.setMemberName(memberEntity.getMemberName());
            model.addAttribute("member", member);
        });
    }

    // 화면 분기
    return (lId != null) ? "chat/lChating" : "chat/gChating";
}
    @PostMapping("/room/read")
    @ResponseBody
    public ResponseEntity<?> markRead(@RequestParam("roomId") Integer roomId,
                                      @RequestParam("who") String who) {
        chatroomService.touchReadAt(roomId, who);
        
        // 뱃지 업데이트 신호 전송
        var room = chatroomService.getRoom(roomId);
        if (room != null) {
            if (room.getMemberIdx() != null) {
                messagingTemplate.convertAndSend("/topic/badge/member/" + room.getMemberIdx(), "update");
            }
            if (room.getLawyerIdx() != null) {
                messagingTemplate.convertAndSend("/topic/badge/lawyer/" + room.getLawyerIdx(), "update");
            }
        }
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/room/deactivate")
    public String deactivate(@RequestParam("roomId") Integer roomId) {
        chatroomService.deactivate(roomId);
        return "redirect:/";
    }

    @PostMapping("/room/end")
    @ResponseBody
    public ResponseEntity<?> endChat(@RequestParam("roomId") Integer roomId,
                                     @RequestParam("who") String who) {
        try {
            chatroomService.endChat(roomId, who);
            
            // 뱃지 업데이트 신호 전송
            var room = chatroomService.getRoom(roomId);
            if (room != null) {
                if (room.getMemberIdx() != null) {
                    messagingTemplate.convertAndSend("/topic/badge/member/" + room.getMemberIdx(), "update");
                }
                if (room.getLawyerIdx() != null) {
                    messagingTemplate.convertAndSend("/topic/badge/lawyer/" + room.getLawyerIdx(), "update");
                }
                
                // 채팅방 상태 변경 신호 전송 (변호사 측에서 textarea 비활성화를 위해)
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/status", Map.of("state", "EXPIRED"));
            }
            
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /* ===================== 커서 기반 히스토리 API ===================== */

    @GetMapping("/api/room/history")
    @ResponseBody
    public List<ChatdataDTO> history(@RequestParam("roomId") Integer roomId,
                                     @RequestParam(name = "beforeId", required = false) Integer beforeId,
                                     @RequestParam(name = "size", defaultValue = "50") int size) {
        if (beforeId == null) return chatDataService.loadLatestBatch(roomId, size);
        return chatDataService.loadHistoryBefore(roomId, beforeId, size);
    }

    /* ===================== 일반회원: 메인 ===================== */

    @GetMapping("/member")
    public String memberMain(@RequestParam(name = "dow", required = false) Integer dow,
                             @RequestParam(name = "duration", defaultValue = "60") int duration,
                             @RequestParam(name = "success", required = false) String successMessage,
                             Model model,
                             @SessionAttribute(value = "loginMember", required = false) Object loginMember,
                             HttpServletResponse resp) throws Exception {

        Integer memberIdx = extractMemberId(loginMember);
        if (memberIdx == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "회원 로그인 필요");
            return null;
        }

        if (dow == null) {
            int jdkDow = java.time.LocalDate.now().getDayOfWeek().getValue(); // 1..7
            dow = (jdkDow + 6) % 7; // 0..6 (월=0)
        }

        model.addAttribute("selectedDow", dow);
        model.addAttribute("duration", duration);
        model.addAttribute("lawyers", calendarService.listLawyersForDayAsMap(dow));
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage);
        }
        return "chat/gMain";
    }

    @PostMapping("/request")
    public String requestChat(@RequestParam("lawyerIdx") Integer lawyerIdx,
                              @RequestParam("durationMinutes") Integer durationMinutes,
                              @SessionAttribute(value = "loginMember", required = false) Object loginMember,
                              HttpServletResponse resp) throws Exception {

        Integer memberIdx = extractMemberId(loginMember);
        if (memberIdx == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "회원 로그인 필요");
            return null;
        }

        if (durationMinutes == null || (durationMinutes != 30 && durationMinutes != 60)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 상담 시간입니다.");
            return null;
        }
        if (!calendarService.canRequestNow(lawyerIdx, durationMinutes)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "현재 시간에는 상담 신청이 불가합니다.");
            return null;
        }

        final int pointCost = (durationMinutes == 30) ? 100 : 200;
        chatroomService.requestChat(memberIdx, lawyerIdx, durationMinutes, pointCost);

        // 변호사가 수락하기 전까지는 채팅방 접근 불가하므로 메인 페이지로 리다이렉트
        // alert로 표시하기 위해 파라미터로 메시지 전달
        String message = URLEncoder.encode("상담 신청이 완료되었습니다. 변호사의 수락을 기다려주세요.", StandardCharsets.UTF_8);
        return "redirect:/chat/member?success=" + message;
    }

   @GetMapping("/api/member/rooms")
    @ResponseBody
    public List<ChatRoomDTO> myRooms(@RequestParam(name = "state", required = false) String state,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size,
                                 @SessionAttribute(value = "loginMember", required = false) Object loginMember) {

    Integer memberIdx = extractMemberId(loginMember);
    if (memberIdx == null) {
        System.out.println("[DEBUG] memberIdx is null");
        return List.of();
    }

    System.out.println("[DEBUG] myRooms called - memberIdx: " + memberIdx + ", state: " + state);

    // state 파라미터가 없거나 ONGOING 이면 진행중(PENDING+ACTIVE)만
    if (state == null || state.isBlank() || "ONGOING".equalsIgnoreCase(state)) {
        List<ChatRoomDTO> rooms = chatroomService.findMemberOngoingRooms(memberIdx, page, size);
        System.out.println("[DEBUG] findMemberOngoingRooms returned: " + rooms.size() + " rooms");
        return rooms;
    }

    // 그 외에는 기존처럼 특정 상태만 (필요하면 유지)
    List<ChatRoomDTO> result = chatroomService.findRoomSummariesForMember(memberIdx, state, page, size);
    System.out.println("[DEBUG] findRoomSummariesForMember returned: " + result.size() + " rooms for state: " + state);
    result.forEach(r -> System.out.println("[DEBUG] Room: " + r.getChatroomIdx() + ", state: " + r.getState() + ", name: " + r.getChatroomName()));
    return result;
}

    @GetMapping("/api/member/rooms/badge")
    @ResponseBody
    public Map<String, Object> myRoomBadges(@SessionAttribute(value = "loginMember", required = false) Object loginMember) {
        Integer memberIdx = extractMemberId(loginMember);
        if (memberIdx == null) return Map.of("pending", 0, "active", 0, "unread", 0);
        return chatroomService.getMemberRoomBadges(memberIdx);
    }

    /* ===================== 변호사: 메인 ===================== */

    @GetMapping("/lawyer")
    public String lawyerMain(Model model,
                             @SessionAttribute(value = "loginLawyer", required = false) Object loginLawyer,
                             HttpServletResponse resp,
                             @RequestParam(name = "page", defaultValue = "0") int page,
                             @RequestParam(name = "size", defaultValue = "20") int size) throws Exception {

        Integer lawyerIdx = extractLawyerId(loginLawyer);
        if (lawyerIdx == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "변호사 로그인 필요");
            return null;
        }

        model.addAttribute("pendingRooms",
                chatroomService.findRoomsForLawyerByState(lawyerIdx, "PENDING", page, size));
        model.addAttribute("activeRooms",
                chatroomService.findRoomsForLawyerByState(lawyerIdx, "ACTIVE", page, size));
        model.addAttribute("endedRooms",
                chatroomService.findRoomsForLawyerByStates(lawyerIdx, List.of("EXPIRED", "CANCELLED"), page, size));
        model.addAttribute("badge",
                chatroomService.getLawyerRoomBadges(lawyerIdx));

        return "chat/lawyerMain";
    }

    @PostMapping("/lawyer/accept")
    public String accept(@RequestParam("roomId") Integer roomId,
                         @SessionAttribute(value = "loginLawyer", required = false) Object loginLawyer,
                         HttpServletResponse resp) throws Exception {
        Integer lawyerIdx = extractLawyerId(loginLawyer);
        if (lawyerIdx == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "변호사 로그인 필요");
            return null;
        }
        chatroomService.accept(roomId, lawyerIdx);
        
        // 뱃지 업데이트 신호 전송
        var room = chatroomService.getRoom(roomId);
        if (room != null) {
            if (room.getMemberIdx() != null) {
                messagingTemplate.convertAndSend("/topic/badge/member/" + room.getMemberIdx(), "update");
            }
            if (room.getLawyerIdx() != null) {
                messagingTemplate.convertAndSend("/topic/badge/lawyer/" + room.getLawyerIdx(), "update");
            }
        }
        
        return "redirect:/chat/lawyer";
    }

    @PostMapping("/lawyer/decline")
    public String decline(@RequestParam("roomId") Integer roomId,
                          @SessionAttribute(value = "loginLawyer", required = false) Object loginLawyer,
                          HttpServletResponse resp) throws Exception {
        Integer lawyerIdx = extractLawyerId(loginLawyer);
        if (lawyerIdx == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "변호사 로그인 필요");
            return null;
        }
        chatroomService.decline(roomId, lawyerIdx);
        return "redirect:/chat/lawyer";
    }

    @GetMapping("/api/lawyer/rooms")
    @ResponseBody
    public List<ChatRoomDTO> lawyerRoomsApi(@RequestParam("stateOrGroup") String stateOrGroup,
                                            @RequestParam(name = "page", defaultValue = "0") int page,
                                            @RequestParam(name = "size", defaultValue = "20") int size,
                                            @SessionAttribute(value = "loginLawyer", required = false) Object loginLawyer) {
        Integer lawyerIdx = extractLawyerId(loginLawyer);
        if (lawyerIdx == null) return List.of();
        if ("ENDED".equalsIgnoreCase(stateOrGroup)) {
            return chatroomService.findRoomsForLawyerByStates(lawyerIdx, List.of("EXPIRED", "CANCELLED"), page, size);
        }
        return chatroomService.findRoomsForLawyerByState(lawyerIdx, stateOrGroup.toUpperCase(), page, size);
    }

    @GetMapping("/api/lawyer/badge")
    @ResponseBody
    public Map<String, Object> lawyerRoomBadges(
            @SessionAttribute(value = "loginLawyer", required = false) Object loginLawyer) {

        Integer lawyerIdx = extractLawyerId(loginLawyer);
        if (lawyerIdx == null) {
            return Map.of("pending", 0, "active", 0, "unread", 0);
        }
        return chatroomService.getLawyerRoomBadges(lawyerIdx);
    }

}
