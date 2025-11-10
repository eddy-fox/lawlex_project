package com.soldesk.team_project.security;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.TemporaryOauthDTO;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.repository.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final HttpServletRequest request;
    private final MemberRepository memberRepository;

    public PrincipalOauth2UserService(HttpServletRequest request, MemberRepository memberRepository) {
        this.request = request;
        this.memberRepository = memberRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = oAuth2User.getAttribute("sub"); // 구글의 유니크 ID
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Optional<MemberEntity> user = memberRepository.findByMemberEmailAndMemberActive(email, 1);

        if (user.isPresent()) {
            // 이미 가입된 유저는 바로 PrincipalDetails 반환
            return new PrincipalDetails(user.get(), oAuth2User.getAttributes());
        } else {
            // 신규 유저는 세션에 임시 정보 저장 후 추가 정보 입력 필요
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                                    .getRequest().getSession();
            TemporaryOauthDTO tempUser = new TemporaryOauthDTO(email, name, provider, providerId);
            session.setAttribute("oauth2TempUser", tempUser);

        throw new OAuth2AuthenticationException("NEED_ADDITIONAL_INFO");
        }
    }
    
}