package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.ui.Model;

import com.soldesk.team_project.dto.ChatRoomDTO;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.service.ChatroomService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HeaderChatModelController {

    private final ChatroomService chatroomService;

    /**
     * ✅ 모든 뷰 렌더링 시 헤더에서 쓸 채팅방 목록을 모델에 자동 추가
     * - 회원 로그인 시: headerMemberRooms
     * - 변호사 로그인 시: headerLawyerRooms
     */
    @ModelAttribute
    public void addHeaderChatRooms(
            Model model,
            @SessionAttribute(value = "loginMember", required = false) MemberDTO loginMember,
            @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer) {

        // 🔹 회원: PENDING + ACTIVE 방 목록
        if (loginMember != null) {
            Integer memberIdx = loginMember.getMemberIdx();
            List<ChatRoomDTO> rooms =
                    chatroomService.findMemberOngoingRooms(memberIdx, 0, 20);
            model.addAttribute("headerMemberRooms", rooms);
        }

        // 🔹 변호사: PENDING + ACTIVE 방 목록 (이미 ChatroomService에 있음)
        if (loginLawyer != null) {
            Integer lawyerIdx = loginLawyer.getLawyerIdx();
            List<String> states = java.util.List.of("PENDING", "ACTIVE");
            List<ChatRoomDTO> rooms =
                    chatroomService.findRoomsForLawyerByStates(lawyerIdx, states, 0, 20);
            model.addAttribute("headerLawyerRooms", rooms);
        }
    }
}
