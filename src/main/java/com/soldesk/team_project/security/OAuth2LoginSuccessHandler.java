package com.soldesk.team_project.security;

import java.io.IOException;

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

        // JSON 응답 생성 (ResponseDTO 없이 간단하게 JWT만 전달)
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(
            new JwtResponse(token, principal.getMember().getMemberEmail(), principal.getMember().getMemberName())
        );

        // 응답 헤더 및 바디 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
        System.out.println(token);
    }

    // JWT 응답용 간단 DTO
    private static class JwtResponse {
        public String token;
        public String email;
        public String username;

        public JwtResponse(String token, String email, String username) {
            this.token = token;
            this.email = email;
            this.username = username;
        }
    }
}