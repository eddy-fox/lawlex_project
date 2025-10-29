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

    @Value("${google.drive.service-account-key-path}")
    private String keyPath;

    @Bean
    public Drive driveService() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = JacksonFactory.getDefaultInstance();

        if (keyPath == null || keyPath.isBlank()) {
            throw new IllegalStateException("google.drive.service-account-key-path is empty");
        }

        try (var in = java.nio.file.Files.newInputStream(java.nio.file.Path.of(keyPath))) {
            var creds = ServiceAccountCredentials.fromStream(in)
                    .createScoped(java.util.List.of("https://www.googleapis.com/auth/drive"));

            return new Drive.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(creds))
                    .setApplicationName("Lawlex")
                    .build();
        }
    }
}
