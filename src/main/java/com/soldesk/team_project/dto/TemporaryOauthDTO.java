package com.soldesk.team_project.dto;

import com.soldesk.team_project.security.UserBase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemporaryOauthDTO implements UserBase{

    private String email;
    private String name;
    private String provider;
    private String providerId;
    
    public TemporaryOauthDTO(String email, String name, String provider, String providerId) {
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
    }

    // UserBase 인터페이스 구현
    @Override
    public Integer getIdx() {
        return null; // 임시 사용자는 ID 없음
    }
    
    @Override
    public String getEmail() {
        return this.email;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public Integer isActive() {
        return 1; // 임시 사용자는 활성 상태로 간주
    }

    @Override
    public String getUserType() {
        return null; // 또는 "temp" - 아직 회원 타입이 결정되지 않음
    }
    
}
