package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.soldesk.team_project.controller.MemberController.MemberSession;
import com.soldesk.team_project.dto.AdDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.NewsBoardEntity;
import com.soldesk.team_project.repository.BoardRepository;
import com.soldesk.team_project.repository.NewsBoardRepository;
import com.soldesk.team_project.service.AdService;
import com.soldesk.team_project.service.CalendarService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AdService adService;
    private final BoardRepository boardRepository;
    private final NewsBoardRepository newsBoardRepository;
    private final CalendarService calendarService;
    
    // 카테고리 상수
    private static final int CATEGORY_NEWS = 2;   // 뉴스
    private static final int CATEGORY_VIDEO = 3;  // 동영상
    private static final int CATEGORY_COLUMN = 4; // 칼럼
    private static final int NEWS_ACTIVE = 1;     // 활성글
    
    @GetMapping({"/", "/member", "/member/"})
    public String home(Model model,
                      @SessionAttribute(value = "loginUser", required = false) UserMasterDTO loginUser,
                      @SessionAttribute(value = "loginMember", required = false) MemberSession loginMember) {
        // 만료된 광고 비활성화
        adService.refreshActiveAds();

        // 활성 광고 조회
        List<AdDTO> adList = adService.getAllAd();
        model.addAttribute("adList", adList);

        // 상담글 조회
        Pageable pageable = PageRequest.of(0, 5);
        List<BoardEntity> topBoards;
        
        // 일반회원 로그인 시 관심 카테고리 기반 인기글 조회
        if (loginUser != null && "MEMBER".equalsIgnoreCase(loginUser.getRole()) 
            && loginMember != null 
            && (loginMember.interestIdx1 != null || loginMember.interestIdx2 != null || loginMember.interestIdx3 != null)) {
            // 관심 카테고리 리스트 생성
            List<Integer> interestIdxList = new java.util.ArrayList<>();
            if (loginMember.interestIdx1 != null) interestIdxList.add(loginMember.interestIdx1);
            if (loginMember.interestIdx2 != null) interestIdxList.add(loginMember.interestIdx2);
            if (loginMember.interestIdx3 != null) interestIdxList.add(loginMember.interestIdx3);
            
            topBoards = boardRepository.findTop5ActiveBoardsByInterestIdxOrderByBoardViewsDesc(
                interestIdxList, 
                pageable);
        } else {
            // 비로그인 또는 관심 카테고리가 없는 경우 전체 인기글 조회
            topBoards = boardRepository.findTop5ActiveBoardsByOrderByBoardViewsDesc(pageable);
        }
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

        // 지금 바로 상담 가능한 변호사 (최대 5개)
        List<java.util.Map<String, Object>> availableLawyers = calendarService.getAvailableLawyersNow();
        model.addAttribute("availableLawyers", availableLawyers);

        return "index";
    }
    
}
