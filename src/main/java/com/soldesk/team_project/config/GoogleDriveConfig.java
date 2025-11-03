package com.soldesk.team_project.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.util.List;

@Configuration
public class GoogleDriveConfig {

    @Value("${google.drive.service-account-key-path}")
    private String keyPath;

    @Bean
    public Drive driveService() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = JacksonFactory.getDefaultInstance();

        var credentials = ServiceAccountCredentials
                .fromStream(new FileInputStream(keyPath))
                .createScoped(List.of("https://www.googleapis.com/auth/drive"));

        return new Drive.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
                .setApplicationName("MyBoardApp")
                .build();
    }
}
