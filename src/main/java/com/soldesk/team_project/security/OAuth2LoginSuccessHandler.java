package com.soldesk.team_project.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        // MemberEntity 기반 JWT 생성
        String token = jwtProvider.createToken(principal.getMember());

        String email = principal.getEmail();
        String name = principal.getUsername();

        // session에 저장
        
        getRedirectStrategy().sendRedirect(request, response, "/");
    }

    // JWT 응답용 간단 DTO
    // private static class JwtResponse {
    //     public String token;
    //     public String email;
    //     public String username;

    //     public JwtResponse(String token, String email, String username) {
    //         this.token = token;
    //         this.email = email;
    //         this.username = username;
    //     }
    // }
}