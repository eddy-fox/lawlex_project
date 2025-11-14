package com.soldesk.team_project.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@Configuration
public class FirebaseStorageConfig {

    @Value("${firebase.configuration-file}")
    private String serviceAccountPath; // C:\\secrets\\....json

    @Value("${firebase.bucket}")
    private String bucketName;         // lawlex-82006.firebasestorage.app

    @Bean
    public FirebaseApp firebaseApp() throws Exception {
        try (InputStream in = new FileInputStream(serviceAccountPath)) {
            GoogleCredentials creds = GoogleCredentials.fromStream(in)
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(creds)
                    .setStorageBucket(bucketName)
                    .build();

            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }
            return FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    public Storage storage() throws Exception {
        // FirebaseApp에서 꺼내지 말고, JSON을 다시 읽어 Storage 클라이언트 생성
        try (InputStream in = new FileInputStream(serviceAccountPath)) {
            GoogleCredentials creds = GoogleCredentials.fromStream(in)
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
            return StorageOptions.newBuilder()
                    .setCredentials(creds)
                    .build()
                    .getService();
        }
    }

    @Bean
    public String firebaseBucketName() {
        return bucketName;
    }
}
