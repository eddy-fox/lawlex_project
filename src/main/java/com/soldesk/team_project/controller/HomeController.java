package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.soldesk.team_project.dto.AdDTO;
import com.soldesk.team_project.service.AdService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AdService adService;
    
    @GetMapping("/")
    public String home(Model model) {
        // 만료된 광고 비활성화
        adService.refreshActiveAds();

        // 활성 광고 조회
        List<AdDTO> adList = adService.getAllAd();

        model.addAttribute("adList", adList);

        return "index";
    }
    
}
