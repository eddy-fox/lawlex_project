package com.soldesk.team_project.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleDriveConfig {

    @Value("${google.drive.credentials-json:}")
    private String credentialsJson;

    @Bean
    public Drive driveService() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = JacksonFactory.getDefaultInstance();

        if (credentialsJson == null || credentialsJson.isBlank()) {
            throw new IllegalStateException("google.drive.credentials-json is empty");
        }

        var credStream = new java.io.ByteArrayInputStream(
                credentialsJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        var creds = ServiceAccountCredentials.fromStream(credStream)
                .createScoped(java.util.List.of("https://www.googleapis.com/auth/drive"));

        return new Drive.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(creds))
                .setApplicationName("Lawlex")
                .build();
    }
}
