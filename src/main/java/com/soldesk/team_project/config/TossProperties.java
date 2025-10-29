package com.soldesk.team_project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TossProperties {
    @Value("${toss.secret-key}")
    private String secretKey;

    public String getSecretKey() {
        return secretKey;
    }

}
