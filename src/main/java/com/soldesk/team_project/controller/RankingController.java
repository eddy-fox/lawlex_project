package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String ranking(@RequestParam(value = "pick", defaultValue = "like") String pick, Model model) {
        List<LawyerDTO> rankingList = rankingService.getRankingList(pick);
        List<java.util.Map<String, Object>> bestAnswers = rankingService.getTopLikedAnswers(10);

        
        model.addAttribute("pick", pick);
        model.addAttribute("rankingList", rankingList);
        model.addAttribute("bestAnswers", bestAnswers);
        /* Map<String, List<Object[]>> rankingMap = rankingService.getInterestAnswerRanking();
        model.addAttribute("rankingMap", rankingMap);  카테고리별 랭킹 사용 안함함  */
        return "rank/ranking";
    }
}
