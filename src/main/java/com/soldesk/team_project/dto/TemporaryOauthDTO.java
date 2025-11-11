package com.soldesk.team_project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemporaryOauthDTO {

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
    
}
