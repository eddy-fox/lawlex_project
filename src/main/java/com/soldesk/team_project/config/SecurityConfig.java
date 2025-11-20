package com.soldesk.team_project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.security.OAuth2LoginSuccessHandler;
import com.soldesk.team_project.security.PrincipalOauth2UserService;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final PrincipalOauth2UserService principalOauth2UserService;

    //private final OAuth2MemberService oAuth2MemberService;

    @Bean
    public UserDetailsService userDetailsService(MemberRepository repo) {
        return username -> {
            MemberEntity m = repo.findByMemberId(username)
                .orElseThrow(() -> new UsernameNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다."));
            if (Integer.valueOf(0).equals(m.getMemberActive())) {
                throw new UsernameNotFoundException("탈퇴한 계정입니다.");
            }
            return User.withUsername(m.getMemberId())
                    .password(m.getMemberPass()) // BCrypt 저장 전제
                    .roles("USER")
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())          // 컨트롤러가 /member/login 직접 처리
            .httpBasic(httpBasic -> httpBasic.disable())
            .logout(logout -> logout.disable())         // 컨트롤러의 /member/logout 사용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/static/**","/css/**","/main.css",
                    "/member/login","/member/loginTemp",
                    "/member/join/**",
                    "/member/api/**","/member/popup/**",
                    "/oauth2/**",
                    "/login/oauth2/code/**"  // OAuth2 콜백 URL 허용
                ).permitAll()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form.disable())
            // oAuth2 로그인
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/member/login")
                .userInfoEndpoint(u -> u.userService(principalOauth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
            );

        return http.build();
        
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 또는 PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }
}
