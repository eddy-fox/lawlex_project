package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.soldesk.team_project.dto.AdDTO;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.NewsBoardEntity;
import com.soldesk.team_project.repository.BoardRepository;
import com.soldesk.team_project.repository.NewsBoardRepository;
import com.soldesk.team_project.service.AdService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AdService adService;
    private final BoardRepository boardRepository;
    private final NewsBoardRepository newsBoardRepository;
    
    // 카테고리 상수
    private static final int CATEGORY_NEWS = 2;   // 뉴스
    private static final int CATEGORY_VIDEO = 3;  // 동영상
    private static final int CATEGORY_COLUMN = 4; // 칼럼
    private static final int NEWS_ACTIVE = 1;     // 활성글
    
    @GetMapping({"/", "/member", "/member/"})
    public String home(Model model) {
        // 만료된 광고 비활성화
        adService.refreshActiveAds();

        // 활성 광고 조회
        List<AdDTO> adList = adService.getAllAd();
        model.addAttribute("adList", adList);

        // 상담글 조회수 높은 순서대로 5개 (활성 게시물만)
        Pageable pageable = PageRequest.of(0, 5);
        List<BoardEntity> topBoards = boardRepository.findTop5ActiveBoardsByOrderByBoardViewsDesc(pageable);
        model.addAttribute("topBoards", topBoards);

        // 동영상 조회수 높은 순서대로 2개
        List<NewsBoardEntity> topVideos = newsBoardRepository
            .findTop5ByCategoryCategoryIdxAndNewsActiveOrderByNewsViewsDesc(
                CATEGORY_VIDEO, NEWS_ACTIVE)
            .stream().limit(2).toList();
        model.addAttribute("topVideos", topVideos);

        // 동영상 최신글 1개
        List<NewsBoardEntity> latestVideo = newsBoardRepository
            .findTop1ByCategoryCategoryIdxAndNewsActiveOrderByNewsRegDateDesc(
                CATEGORY_VIDEO, NEWS_ACTIVE);
        model.addAttribute("latestVideo", latestVideo);

        // 뉴스 조회수 높은 순서
        List<NewsBoardEntity> popularNews = newsBoardRepository
            .findTop5ByCategoryCategoryIdxAndNewsActiveOrderByNewsViewsDesc(
                CATEGORY_NEWS, NEWS_ACTIVE);
        model.addAttribute("popularNews", popularNews);

        // 뉴스 최신글
        List<NewsBoardEntity> latestNewsList = newsBoardRepository
            .findByCategoryCategoryIdxAndNewsActiveOrderByNewsIdxDesc(CATEGORY_NEWS, NEWS_ACTIVE)
            .stream().limit(5).toList();
        model.addAttribute("latestNews", latestNewsList);

        return "index";
    }
    
}
