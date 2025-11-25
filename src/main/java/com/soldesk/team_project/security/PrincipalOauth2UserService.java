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

import com.soldesk.team_project.dto.TemporaryOauthDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.security.oauth.GoogleUserInfo;
import com.soldesk.team_project.security.oauth.NaverUserInfo;
import com.soldesk.team_project.security.oauth.OAuth2UserInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final HttpServletRequest request;
    private final MemberRepository memberRepository;
    private final LawyerRepository lawyerRepository;

    public PrincipalOauth2UserService(HttpServletRequest request, 
        MemberRepository memberRepository, LawyerRepository lawyerRepository) {
        this.request = request;
        this.memberRepository = memberRepository;
        this.lawyerRepository = lawyerRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google", "naver"
        
        OAuth2UserInfo oauth2UserInfo = null;
        
        if (provider.equals("google")) {
            oauth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (provider.equals("naver")) {
            oauth2UserInfo = new NaverUserInfo((Map) oAuth2User.getAttributes().get("response"));
        }

        if (oauth2UserInfo == null) {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다.");
        }

        String providerId = oauth2UserInfo.getProviderId();
        String email = oauth2UserInfo.getEmail();
        String name = oauth2UserInfo.getName();

        Optional<MemberEntity> memberUser = memberRepository.findByMemberProviderAndMemberProviderIdAndMemberActive(provider, providerId, 1);
        Optional<LawyerEntity> lawyerUser = lawyerRepository.findByLawyerProviderAndLawyerProviderIdAndLawyerActive(provider, providerId, 1);

         if (memberUser.isEmpty() && lawyerUser.isEmpty()) {
            TemporaryOauthDTO temp = new TemporaryOauthDTO(email, name, provider, providerId);

            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                                    .getRequest().getSession();
            session.setAttribute("tempOauth", temp);

            return new PrincipalDetails(temp, oAuth2User.getAttributes());
        } else if (memberUser.isPresent() && lawyerUser.isEmpty()){
            return new PrincipalDetails(memberUser.get(), oAuth2User.getAttributes());
        } else {
            return new PrincipalDetails(lawyerUser.get(), oAuth2User.getAttributes());
        }
    }
}