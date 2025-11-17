package com.soldesk.team_project.security;

import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.TemporaryOauthDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;

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

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = oAuth2User.getAttribute("sub"); // 구글의 유니크 ID
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Optional<MemberEntity> memberUser = memberRepository.findByMemberProviderAndMemberProviderId(provider, providerId);
        Optional<LawyerEntity> lawyerUser = lawyerRepository.findByLawyerProviderAndLawyerProviderId(provider, providerId);

         if (memberUser.isEmpty() && lawyerUser.isEmpty()) {
            TemporaryOauthDTO temp = new TemporaryOauthDTO(email, name, provider, providerId);
            temp.setEmail(email);
            temp.setName(name);
            temp.setProvider(provider);
            temp.setProviderId(providerId);

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