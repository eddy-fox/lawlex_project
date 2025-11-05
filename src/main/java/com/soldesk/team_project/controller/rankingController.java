package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.service.RankingService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/rank")
@RequiredArgsConstructor
public class RankingController {
    
    private final RankingService rankingService;

    // 랭킹 페이지
    @GetMapping("/ranking")
    public String ranking(Model model) {
        // String pick = "like";
        // List<LawyerDTO> rankingList = rankingService.getRankingList(pick);
        // model.addAttribute("rankingList", rankingList);
        return "rank/ranking";
    }
    
    
}
