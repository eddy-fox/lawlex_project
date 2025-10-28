package com.soldesk.team_project.controller;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;


@Controller
public class TemporaryController {

    @GetMapping("/all/chat/lawyerMain")
    public String lawyerMain(){
        return "/all/chat/lawyerMain";
    }
    @GetMapping("/all/chat/gMain")
    public String gMain(){
        return "/all/chat/gMain";
    }
    @GetMapping("/all/board/write")
    public String write(){
        return "/all/board/write";
    }
    @GetMapping("/all/board/modify")
    public String modify(){
        return "/all/board/modify";
    }
    @GetMapping("/all/board/list")
    public String list(){
        return "/all/board/list";
    }
    @GetMapping("/all/board/reBoard")
    public String reBoard(){
        return "/all/board/reBoard";
    }
    @GetMapping("/all/admin/memberManagement")
    public String memberManagement(){
        return "/all/admin/memberManagement";
    }
    @GetMapping("/all/admin/QnAManagement")
    public String QnAManagement(){
        return "/all/admin/QnAManagement";
    }
    @GetMapping("/all/admin/adInfo")
    public String adInfo(){
        return "/all/admin/adInfo";
    }
    @GetMapping("/all/admin/adList")
    public String adList(){
        return "/all/admin/adList";
    }
    @GetMapping("/all/member/point")
    public String point(){
        return "/all/member/point";
    }
    @GetMapping("/all/member/login")
    public String login(){
        return "/all/member/login";
    }
    @GetMapping("/all/member/loginFind")
    public String loginFind(){
        return "/all/member/loginFind";
    }
    @GetMapping("/all/member/loginChoice")
    public String loginChoice(){
        return "/all/member/loginChoice";
    }
    @GetMapping("/all/member/gJoin")
    public String gJoin(){
        return "/all/member/gJoin";
    }
    @GetMapping("/all/member/lJoin")
    public String lJoin(){
        return "/all/member/ljoin";
    }
    @GetMapping("/all/member/gInfo")
    public String gInfo(){
        return "/all/member/gInfo";
    }
    @GetMapping("/all/member/lInfo")
    public String lInfo(){
        return "/all/member/lInfo";
    }
    @GetMapping("/all/member/gModify")
    public String gModify(){
        return "/all/member/gModify";
    }
    @GetMapping("/all/member/lModify")
    public String lModify(){
        return "/all/member/lModify";
    }
            // 간단 캐시: "aAll" -> "member/aAll" 처럼 저장
    private static final ConcurrentHashMap<String, String> VIEW_CACHE = new ConcurrentHashMap<>();

    @GetMapping("/aAll")
    public String aAll() {
        return resolveViewByScan("aAll");
    }
    @GetMapping("/html")
    public String html() {
        return resolveViewByScan("html");
    }

    // --- 핵심 유틸: 템플릿 스캔해서 뷰이름 도출 ---
    private String resolveViewByScan(String simpleName) {
        // 캐시에 있으면 바로 반환
        String cached = VIEW_CACHE.get(simpleName);
        if (StringUtils.hasText(cached)) return cached;

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            // templates/ 하위 모든 폴더에서 simpleName.html 검색
            String pattern = "classpath:/templates/**/" + simpleName + ".html";
            Resource[] resources = resolver.getResources(pattern);

            if (resources.length == 0) {
                // 없으면 404
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Template not found: **/" + simpleName + ".html");
            }

            // 일단 첫 번째 매치 사용 (필요하면 우선순위 규칙 추가)
            Resource r = resources[0];
            URI uri = r.getURI(); // jar 내 리소스도 인식
            String uriStr = uri.toString();

            // "…/templates/" 이후 경로 + 확장자 제거 -> 뷰이름
            String marker = "/templates/";
            int idx = uriStr.indexOf(marker);
            if (idx == -1) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Cannot derive view name from URI: " + uriStr);
            }
            String after = uriStr.substring(idx + marker.length());   // e.g. "member/aAll.html"
            String viewName = after.replace(".html", "");             // -> "member/aAll"

            // 캐시 저장
            VIEW_CACHE.put(simpleName, viewName);
            return viewName;

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Template scan failed for: **/" + simpleName + ".html", e);
        }
    }
}
