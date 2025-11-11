package com.soldesk.team_project.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.soldesk.team_project.entity.MemberEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final MemberEntity member;
    private Map<String, Object> attributes;

    // 일반 로그인용 생성자
    // public PrincipalDetails() {
    //     this.member = member;
    // }

    // OAuth 로그인용 생성자
    public PrincipalDetails(MemberEntity member, Map<String, Object> attributes) {
        this.member = member;
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
        return member.getMemberName(); // OAuth2User.getName()용
    }

    // UserDetails 인터페이스 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // member 권한 반환 (예: "ROLE_MEMBER")
        return List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));
    }

    @Override
    public String getPassword() {
        return member.getMemberPass();
    }

    @Override
    public String getUsername() {
        return member.getMemberEmail(); // 로그인 ID용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return member.getMemberActive() == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return member.getMemberActive() == 1;
    }

    // 편의 메서드
    public Integer getMemberIdx() {
        return member.getMemberIdx();
    }

    public String getEmail() {
        return member.getMemberEmail();
    }

    public String getMemberName() {
        return member.getMemberName();
    }
}