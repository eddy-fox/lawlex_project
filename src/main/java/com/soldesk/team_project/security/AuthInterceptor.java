package com.soldesk.team_project.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.entity.AdminEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.repository.AdminRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.security.JwtProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final LawyerRepository lawyerRepository;
    private final AdminRepository adminRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        HttpSession session = request.getSession(false);
        
        // 세션에 이미 loginUser가 있으면 (로컬 로그인) 그대로 통과
        if (session != null && session.getAttribute("loginUser") != null) {
            return true;
        }

        // 쿠키에서 JWT 토큰 찾기
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return true; // 쿠키가 없으면 통과 (인증 불필요한 요청일 수 있음)
        }

        String jwtToken = null;
        for (Cookie cookie : cookies) {
            if ("jwtToken".equals(cookie.getName())) {
                jwtToken = cookie.getValue();
                break;
            }
        }

        // JWT 토큰이 없으면 통과
        if (jwtToken == null || jwtToken.isEmpty()) {
            return true;
        }

        // JWT 검증 및 파싱
        Integer idx = jwtProvider.getIdxFromToken(jwtToken);
        String userType = jwtProvider.getUserTypeFromToken(jwtToken);

        if (idx == null || userType == null) {
            log.warn("JWT 토큰 검증 실패 또는 만료: {}", jwtToken);
            // 만료된 쿠키 삭제
            Cookie expiredCookie = new Cookie("jwtToken", null);
            expiredCookie.setPath("/");
            expiredCookie.setMaxAge(0);
            response.addCookie(expiredCookie);
            return true; // 통과 (인증 실패해도 요청은 진행, 컨트롤러에서 처리)
        }

        // userType에 따라 UserMasterDTO 생성
        UserMasterDTO userMasterDTO = null;

        try {
            if ("member".equalsIgnoreCase(userType)) {
                MemberEntity member = memberRepository.findById(idx).orElse(null);
                if (member != null && member.getMemberActive() != null && member.getMemberActive() == 1) {
                    userMasterDTO = UserMasterDTO.builder()
                            .userId(member.getMemberId())
                            .role("MEMBER")
                            .memberIdx(member.getMemberIdx())
                            .lawyerIdx(null)
                            .adminIdx(null)
                            .build();
                }
            } else if ("lawyer".equalsIgnoreCase(userType)) {
                LawyerEntity lawyer = lawyerRepository.findById(idx).orElse(null);
                if (lawyer != null) {
                    userMasterDTO = UserMasterDTO.builder()
                            .userId(lawyer.getLawyerId())
                            .role("LAWYER")
                            .memberIdx(null)
                            .lawyerIdx(lawyer.getLawyerIdx())
                            .adminIdx(null)
                            .build();
                }
            } else if ("admin".equalsIgnoreCase(userType)) {
                AdminEntity admin = adminRepository.findById(idx).orElse(null);
                if (admin != null) {
                    userMasterDTO = UserMasterDTO.builder()
                            .userId(admin.getAdminId())
                            .role("ADMIN")
                            .memberIdx(null)
                            .lawyerIdx(null)
                            .adminIdx(admin.getAdminIdx())
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("UserMasterDTO 생성 중 오류 발생: {}", e.getMessage());
            return true; // 오류 발생해도 통과
        }

        // UserMasterDTO를 세션에 저장 (로컬 로그인과 동일한 방식)
        if (userMasterDTO != null) {
            if (session == null) {
                session = request.getSession(true);
            }
            session.setAttribute("loginUser", userMasterDTO);
            log.debug("JWT에서 UserMasterDTO 생성 및 세션 저장 완료: userId={}, role={}", 
                    userMasterDTO.getUserId(), userMasterDTO.getRole());
        }

        return true;
    }
}

