package com.soldesk.team_project.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
<<<<<<< HEAD
=======
import com.soldesk.team_project.dto.TemporaryOauthDTO;
>>>>>>> main

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // OAuth2 로그인 완료 후 Principal 가져오기
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        HttpSession session = request.getSession();

        TemporaryOauthDTO temp = (session != null) ? (TemporaryOauthDTO) session.getAttribute("tempOauth") : null;

        if (temp != null) {
            // 신규 사용자 → 회원 유형 선택 페이지로 리디렉트
            getRedirectStrategy().sendRedirect(request, response, "/member/loginChoice");
        } else {
            // 기존 사용자 → JWT 생성 후 메인 페이지로 리디렉트
            String token = jwtProvider.createToken(principal.getUser());
            String redirectUrl = "http://localhost:8080/?token=" + token;
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }

    }

}