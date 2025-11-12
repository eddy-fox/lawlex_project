package com.soldesk.team_project.controller;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.soldesk.team_project.dto.ChatRoomDTO;
import com.soldesk.team_project.dto.ChatdataDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.LawyerDTO;
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


        @GetMapping("/chat")
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
        if (sessionObj instanceof MemberDTO m) {
            return m.getMemberIdx();
        }
        // MemberController 안에 있는 내부 클래스인 경우
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
        if (sessionObj instanceof LawyerDTO l) {
            return l.getLawyerIdx();
        }
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

    /**
     * 채팅방 화면
     * - 회원이면 chat/gChating.html
     * - 변호사면 chat/lChating.html
     */
    @GetMapping("/room")
    public String room(@RequestParam Integer roomId,
                       @RequestParam(defaultValue = "50") int size,
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
        List<ChatdataDTO> initBatch = chatDataService.loadLatestBatch(roomId, size);
        model.addAttribute("room", room);
        model.addAttribute("initBatch", initBatch);

        // 누가 들어왔는지에 따라 다른 html
        Integer mId = extractMemberId(loginMember);
        Integer lId = extractLawyerId(loginLawyer);
        if (lId != null) {
            return "chat/lChating";
        } else {
            // 기본을 회원 화면으로
            return "chat/gChating";
        }
    }

    @PostMapping("/room/read")
    @ResponseBody
    public ResponseEntity<?> markRead(@RequestParam Integer roomId, @RequestParam String who) {
        chatroomService.touchReadAt(roomId, who);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/room/deactivate")
    public String deactivate(@RequestParam Integer roomId) {
        chatroomService.deactivate(roomId);
        return "redirect:/";
    }

    /* ===================== 커서 기반 히스토리 API ===================== */

    @GetMapping("/api/room/history")
    @ResponseBody
    public List<ChatdataDTO> history(@RequestParam Integer roomId,
                                     @RequestParam(required = false) Integer beforeId,
                                     @RequestParam(defaultValue = "50") int size) {
        if (beforeId == null) {
            return chatDataService.loadLatestBatch(roomId, size);
        }
        return chatDataService.loadHistoryBefore(roomId, beforeId, size);
    }

    /* ===================== 일반회원: 메인 ===================== */

    @GetMapping("/member")
    public String memberMain(@RequestParam(name = "dow", required = false) Integer dow,
                             @RequestParam(name = "duration", defaultValue = "60") int duration,
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
            dow = (jdkDow + 6) % 7; // 0..6
        }

        model.addAttribute("selectedDow", dow);
        model.addAttribute("duration", duration);
        model.addAttribute("lawyers", calendarService.listLawyersForDayAsMap(dow));

        // ✅ 일반회원 메인 html 이름
        return "chat/gMain";
    }

    @PostMapping("/request")
    public String requestChat(@RequestParam Integer lawyerIdx,
                              @RequestParam Integer durationMinutes,
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
        ChatRoomDTO room = chatroomService.requestChat(
                memberIdx, lawyerIdx, durationMinutes, pointCost);

        return "redirect:/chat/room?roomId=" + room.getChatroomIdx();
    }

    @GetMapping("/api/member/rooms")
    @ResponseBody
    public List<ChatRoomDTO> myRooms(@RequestParam(name = "state", required = false) String state,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @SessionAttribute(value = "loginMember", required = false) Object loginMember) {
        Integer memberIdx = extractMemberId(loginMember);
        if (memberIdx == null) return List.of();
        return chatroomService.findRoomSummariesForMember(memberIdx, state, page, size);
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
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) throws Exception {

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

        // ✅ 변호사 메인 html 이름
        return "chat/lawyerMain";
    }

    @PostMapping("/lawyer/accept")
    public String accept(@RequestParam Integer roomId,
                         @SessionAttribute(value = "loginLawyer", required = false) Object loginLawyer,
                         HttpServletResponse resp) throws Exception {
        Integer lawyerIdx = extractLawyerId(loginLawyer);
        if (lawyerIdx == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "변호사 로그인 필요");
            return null;
        }
        chatroomService.accept(roomId, lawyerIdx);
        return "redirect:/chat/lawyer";
    }

    @PostMapping("/lawyer/decline")
    public String decline(@RequestParam Integer roomId,
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
    public List<ChatRoomDTO> lawyerRoomsApi(@RequestParam String stateOrGroup,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @SessionAttribute(value = "loginLawyer", required = false) Object loginLawyer) {
        Integer lawyerIdx = extractLawyerId(loginLawyer);
        if (lawyerIdx == null) return List.of();
        if ("ENDED".equalsIgnoreCase(stateOrGroup)) {
            return chatroomService.findRoomsForLawyerByStates(lawyerIdx, List.of("EXPIRED", "CANCELLED"), page, size);
        }
        return chatroomService.findRoomsForLawyerByState(lawyerIdx, stateOrGroup.toUpperCase(), page, size);
    }
}
