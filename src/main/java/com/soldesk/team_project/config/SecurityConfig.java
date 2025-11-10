// package com.soldesk.team_project.config;

// import lombok.RequiredArgsConstructor;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.Customizer;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;

// import com.soldesk.team_project.entity.MemberEntity;
// import com.soldesk.team_project.repository.MemberRepository;
// import com.soldesk.team_project.service.OAuth2MemberService;

// @Configuration
// @RequiredArgsConstructor
// public class SecurityConfig {

//     private final OAuth2MemberService oAuth2MemberService;

//     @Bean
//     public UserDetailsService userDetailsService(MemberRepository repo) {
//         return username -> {
//             MemberEntity m = repo.findByMemberId(username)
//                 .orElseThrow(() -> new UsernameNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다."));
//             if (Integer.valueOf(0).equals(m.getMemberActive())) {
//                 throw new UsernameNotFoundException("탈퇴한 계정입니다.");
//             }
//             return User.withUsername(m.getMemberId())
//                     .password(m.getMemberPass()) // BCrypt 저장 전제
//                     .roles("USER")
//                     .build();
//         };
//     }

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http.csrf(csrf -> csrf.disable())
//             .authorizeHttpRequests(auth -> auth
//                 .requestMatchers(
//                     "/static/**","/css/**","/main.css",
//                     "/member/login","/member/join/**",
//                     "/member/api/**","/member/popup/**",
//                     "/oauth2/**"
//                 ).permitAll()
//                 .anyRequest().permitAll()
//             )
//             .formLogin(form -> form
//                 .loginPage("/member/login")
//                 .loginProcessingUrl("/member/login")
//                 .usernameParameter("username")
//                 .passwordParameter("password")
//                 .defaultSuccessUrl("/member/main", true)
//                 .failureUrl("/member/login?error")
//             )
//             // .oauth2Login(oauth -> oauth
//             //     .loginPage("/member/login")
//             //     .userInfoEndpoint(u -> u.userService(oAuth2MemberService))
//             //     .defaultSuccessUrl("/member/main", true)
//             // )
//             .logout(l -> l.logoutUrl("/member/logout").logoutSuccessUrl("/member/login"))
//             .httpBasic(Customizer.withDefaults());

//         return http.build();
        
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder(); // 또는 PasswordEncoderFactories.createDelegatingPasswordEncoder()
//     }
// }
