package com.soldesk.team_project.controller;

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

    /* ===================== 공통: 방 입장/읽음/비활성 ===================== */

    /**
     * 채팅방 화면
     * - 변호사 로그인 → chat/lChating.html
     * - 일반회원 로그인 → chat/gChating.html
     */
    @GetMapping("/room")
    public String room(@RequestParam Integer roomId,
                       @RequestParam(defaultValue = "50") int size,
                       Model model,
                       @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
                       @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer,
                       HttpServletResponse resp) throws Exception {

        ChatRoomDTO room = chatroomService.getRoom(roomId);
        if (room == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "존재하지 않는 채팅방");
            return null;
        }

        List<ChatdataDTO> initBatch = chatDataService.loadLatestBatch(roomId, size); // ASC로 반환
        model.addAttribute("room", room);
        model.addAttribute("initBatch", initBatch);

        // 변호사/회원에 따라 다른 템플릿
        if (loginLawyer != null) {
            return "chat/lChating";   // templates/chat/lChating.html
        } else {
            return "chat/gChating";   // templates/chat/gChating.html
        }
    }

    /** 읽음 시각 갱신 (who = MEMBER | LAWYER) */
    @PostMapping("/room/read")
    @ResponseBody
    public ResponseEntity<?> markRead(@RequestParam Integer roomId, @RequestParam String who) {
        chatroomService.touchReadAt(roomId, who);
        return ResponseEntity.ok().build();
    }

    /** 방 비활성화(나가기) */
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

    /* ===================== 일반회원: 메인(변호사 카드) & 신청 ===================== */

    /** 일반회원 메인 */
    @GetMapping("/member")
    public String memberMain(@RequestParam(name = "dow", required = false) Integer dow,            // 0=Mon..6=Sun
                             @RequestParam(name = "duration", defaultValue = "60") int duration,   // 30 or 60
                             Model model,
                             @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
                             HttpServletResponse resp) throws Exception {
        if (loginMember == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "회원 로그인 필요");
            return null;
        }
        if (dow == null) {
            int jdkDow = java.time.LocalDate.now().getDayOfWeek().getValue(); // Mon=1..Sun=7
            dow = (jdkDow + 6) % 7; // → Mon0..Sun6
        }
        model.addAttribute("selectedDow", dow);
        model.addAttribute("duration",    duration);
        // 이 메서드는 네가 calendarService에 만든 이름과 맞춰야 한다
        model.addAttribute("lawyers", calendarService.listLawyersForDayAsMap(dow));

        return "chat/gMain"; // templates/chat/gMain.html
    }

    /** 회원 → 변호사 채팅 신청 */
    @PostMapping("/request")
    public String requestChat(@RequestParam Integer lawyerIdx,
                              @RequestParam Integer durationMinutes, // 30 or 60
                              @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
                              HttpServletResponse resp) throws Exception {
        if (loginMember == null) {
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
                loginMember.getMemberIdx(), lawyerIdx, durationMinutes, pointCost);

        return "redirect:/chat/room?roomId=" + room.getChatroomIdx();
    }

    /** 헤더 아이콘: 내 방 요약 목록(Ajax) */
    @GetMapping("/api/member/rooms")
    @ResponseBody
    public List<ChatRoomDTO> myRooms(@RequestParam(name = "state", required = false) String state, // PENDING/ACTIVE/null=all
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember) {
        if (loginMember == null) return List.of();
        return chatroomService.findRoomSummariesForMember(loginMember.getMemberIdx(), state, page, size);
    }

    /** 헤더 아이콘: 뱃지(Ajax) */
    @GetMapping("/api/member/rooms/badge")
    @ResponseBody
    public Map<String, Object> myRoomBadges(@SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember) {
        if (loginMember == null) return Map.of("pending", 0, "active", 0, "unread", 0);
        return chatroomService.getMemberRoomBadges(loginMember.getMemberIdx());
    }

    /* ===================== 변호사: 메인(대기/진행/종료) & 수락/거절 ===================== */

    /** 변호사 메인 페이지 */
    @GetMapping("/lawyer")
    public String lawyerMain(Model model,
                             @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer,
                             HttpServletResponse resp,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) throws Exception {
        if (loginLawyer == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "변호사 로그인 필요");
            return null;
        }
        Integer lawyerIdx = loginLawyer.getLawyerIdx();

        model.addAttribute("pendingRooms",
                chatroomService.findRoomsForLawyerByState(lawyerIdx, "PENDING", page, size));
        model.addAttribute("activeRooms",
                chatroomService.findRoomsForLawyerByState(lawyerIdx, "ACTIVE",  page, size));
        model.addAttribute("endedRooms",
                chatroomService.findRoomsForLawyerByStates(lawyerIdx, List.of("EXPIRED", "CANCELLED"), page, size));
        model.addAttribute("badge",
                chatroomService.getLawyerRoomBadges(lawyerIdx));

        return "chat/lawyerMain"; // templates/chat/lawyerMain.html
    }

    /** 대기중 상담 수락 */
    @PostMapping("/lawyer/accept")
    public String accept(@RequestParam Integer roomId,
                         @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer,
                         HttpServletResponse resp) throws Exception {
        if (loginLawyer == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "변호사 로그인 필요");
            return null;
        }
        chatroomService.accept(roomId, loginLawyer.getLawyerIdx());
        return "redirect:/chat/lawyer";
    }

    /** 대기중 상담 거절 */
    @PostMapping("/lawyer/decline")
    public String decline(@RequestParam Integer roomId,
                          @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer,
                          HttpServletResponse resp) throws Exception {
        if (loginLawyer == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "변호사 로그인 필요");
            return null;
        }
        chatroomService.decline(roomId, loginLawyer.getLawyerIdx());
        return "redirect:/chat/lawyer";
    }

    /** (선택) 변호사 메인 섹션 더보기 Ajax */
    @GetMapping("/api/lawyer/rooms")
    @ResponseBody
    public List<ChatRoomDTO> lawyerRoomsApi(@RequestParam String stateOrGroup, // PENDING | ACTIVE | ENDED
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer) {
        if (loginLawyer == null) return List.of();
        Integer lawyerIdx = loginLawyer.getLawyerIdx();
        if ("ENDED".equalsIgnoreCase(stateOrGroup)) {
            return chatroomService.findRoomsForLawyerByStates(lawyerIdx, List.of("EXPIRED", "CANCELLED"), page, size);
        }
        return chatroomService.findRoomsForLawyerByState(lawyerIdx, stateOrGroup.toUpperCase(), page, size);
    }
}
