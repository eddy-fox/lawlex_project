package com.soldesk.team_project.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FirebaseStorageService {

    private final Storage storage;

    @Value("${firebase.bucket}")
    private String bucket; // lawlex-82006.firebasestorage.app

    private static final String PUBLIC_BASE =
            "https://storage.googleapis.com"; // 공개 읽기 기준

    /** 업로드 (MultipartFile) */
    public UploadedObject upload(MultipartFile file, String objectPath) {
        try {
            String contentType = Optional.ofNullable(file.getContentType())
                    .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            BlobId blobId = BlobId.of(bucket, objectPath);             // e.g. news/제목.jpg
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();

            storage.create(blobInfo, file.getBytes());

            String url = buildPublicUrl(objectPath);                   // 공개 버킷이면 바로 사용 가능
            return new UploadedObject(objectPath, url);
        } catch (Exception e) {
            throw new RuntimeException("Firebase upload failed: " + objectPath, e);
        }
    }

    /** 업로드 (raw bytes) */
    public UploadedObject upload(byte[] data, String objectPath, String contentType) {
        try {
            BlobId blobId = BlobId.of(bucket, objectPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(Optional.ofNullable(contentType)
                            .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .build();
            storage.create(blobInfo, data);
            return new UploadedObject(objectPath, buildPublicUrl(objectPath));

        } catch (Exception e) {
            throw new RuntimeException("Firebase upload failed: " + objectPath, e);
        }
    }

    /** 존재 여부 */
    public boolean exists(String objectPath) {
        return storage.get(BlobId.of(bucket, objectPath)) != null;
    }

    /** 삭제 */
    public boolean delete(String objectPath) {
        return storage.delete(BlobId.of(bucket, objectPath));
    }

    /** 공개 URL 생성 (버킷이 allUsers:objectViewer일 때 즉시 사용 가능) */
    public String buildPublicUrl(String objectPath) {
    String encoded = encodePath(objectPath); // 슬래시는 살리고, 한글/공백 인코딩
    return "https://storage.googleapis.com/" + bucket + "/" + encoded;
}

    /** (선택) 서명 URL – 버킷을 공개로 열지 않았을 때 사용 */
    // 사용 전: 서비스 계정에 sign 권한 필요
    /*
    public String buildSignedUrl(String objectPath, Duration ttl) {
        URL url = storage.signUrl(
            BlobInfo.newBuilder(bucket, objectPath).build(),
            ttl.toSeconds(), TimeUnit.SECONDS,
            Storage.SignUrlOption.withV4Signature());
        return url.toString();
    }
    */

    /** news/허위 제목.jpg 처럼 한글/공백 포함 경로 안전 인코딩 */
    private String encodePath(String path) {
        String[] parts = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append('/');
            sb.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8)
                    .replace("+", "%20")); // 공백을 %20으로
        }
        return sb.toString();
    }

    /** 업로드 결과 DTO */
    public record UploadedObject(String fileId, String url) {}
}
