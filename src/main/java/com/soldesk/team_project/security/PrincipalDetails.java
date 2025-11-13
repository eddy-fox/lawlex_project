package com.soldesk.team_project.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final UserBase user; // MemberEntity or LawyerEntity 모두 가능
    private Map<String, Object> attributes;

    // OAuth 로그인용 생성자
    public PrincipalDetails(UserBase user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // OAuth2User 인터페이스 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public <A> A getAttribute(String name) {
        if (attributes != null) {
            return (A) attributes.get(name);
        }
        return null;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    // UserDetails 인터페이스 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_MEMBER")); // 필요시 ROLE 구분 추가
    }

    @Override
    public String getPassword() {
        // OAuth는 패스워드 없을 수 있음
        return null;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (user.isActive() == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (user.isActive() == 0) {
            return false;
        } else {
            return true;
        }
    }

    // 편의 메서드
    public Integer getUserId() {
        return user.getIdx();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getUserName() {
        return user.getName();
    }
}
