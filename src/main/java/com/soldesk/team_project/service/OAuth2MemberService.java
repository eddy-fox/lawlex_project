// package com.soldesk.team_project.service;

// import jakarta.servlet.http.HttpSession;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
<<<<<<< HEAD
// import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
// import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
// import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
// import org.springframework.security.oauth2.core.user.OAuth2User;
=======
>>>>>>> main
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.soldesk.team_project.entity.MemberEntity;
// import com.soldesk.team_project.repository.MemberRepository;

// import java.util.*;


// @Service
// @RequiredArgsConstructor
<<<<<<< HEAD
// public class OAuth2MemberService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
=======
// public class OAuth2MemberService implements OAuth2UserService<OAuth2UserRequest, oAuth2User> {
>>>>>>> main

//     private final MemberRepository memberRepository;
//     private final HttpSession session;

//     @Override
//     @Transactional
<<<<<<< HEAD
//     public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
=======
//     public oauth2 loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
>>>>>>> main
//         OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
//         OAuth2User oAuth2User = delegate.loadUser(req);

//         String registrationId = req.getClientRegistration().getRegistrationId(); // google | naver
//         Map<String, Object> attributes = new LinkedHashMap<>(oAuth2User.getAttributes());

//         String providerUserId;
//         String email = null;
//         String name = null;

//         if ("google".equals(registrationId)) {
//             providerUserId = String.valueOf(attributes.get("sub"));
//             email = (String) attributes.get("email");
//             name = (String) attributes.get("name");

//         } else if ("naver".equals(registrationId)) {
//             // {resultcode:"00", message:"success", response:{id:"...", email:"...",
//             // name:"..."}}
//             @SuppressWarnings("unchecked")
//             Map<String, Object> resp = (Map<String, Object>) attributes.get("response");
//             providerUserId = String.valueOf(resp.get("id"));
//             email = (String) resp.get("email");
//             name = (String) resp.get("name");
//             // 평탄화(템플릿에서 바로 쓰기 좋게)
//             attributes.putAll(resp);

//         } else {
//             throw new OAuth2AuthenticationException(
//                     new OAuth2Error("unsupported_provider"), "Unsupported provider");
//         }

//         // memberId = {provider}_{id}
//         String memberId = registrationId + "_" + providerUserId;

//         // ★ 람다 캡처용 final 복사 (effectively final 요구 해결)
//         final String memberIdF = memberId;
//         final String emailF = email;
//         final String nameF = name;
//         final String registrationIdF = registrationId;

//         // 자동가입 or 기존회원 조회
//         MemberEntity me = memberRepository.findByMemberId(memberIdF).orElseGet(() -> {
//             MemberEntity newbie = MemberEntity.builder()
//                     .memberId(memberIdF)
//                     .memberPass("{noop}oauth2") // 폼로그인 비번과 별개
//                     .memberName(Optional.ofNullable(nameF).orElse(registrationIdF))
//                     .memberEmail(emailF)
//                     .memberNickname(registrationIdF + "_user")
//                     .memberPhone(null)
//                     .memberIdnum(null)
//                     .memberActive(1)
//                     .build();
//             return memberRepository.save(newbie);
//         });

//         // v13 세션 방식: memberIdx 저장
//         session.setAttribute("memberIdx", me.getMemberIdx());

//         // SecurityContext에 넣을 OAuth2User
//         return new DefaultOAuth2User(
//                 Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
//                 attributes,
//                 "google".equals(registrationId) ? "sub" : "id");
//     }
// }
