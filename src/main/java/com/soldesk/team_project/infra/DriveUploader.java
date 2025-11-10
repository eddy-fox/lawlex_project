package com.soldesk.team_project.infra;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class DriveUploader {

    private final Drive drive;

    @Value("${google.drive.make-public:true}")
    private boolean makePublic;

    private static String convertFileName(MultipartFile multipart) {
        String originalFileName = multipart.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            originalFileName = "upload";
        }

        String ext = "";
        int dot = originalFileName.lastIndexOf('.');
        if (dot != -1 && dot < originalFileName.length() - 1) {
            ext = originalFileName.substring(dot);
        }

        String base = originalFileName
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .trim();
        if (base.isEmpty()) base = "upload";

        String storedFileName = System.currentTimeMillis() + "_" + base;
        return storedFileName + ext;
    }

    public UploadedFileInfo upload(MultipartFile multipart, String parentFolderId) throws Exception {
        File metadata = new File();
        metadata.setName(convertFileName(multipart));
        metadata.setParents(java.util.List.of(parentFolderId));

        ByteArrayContent content = new ByteArrayContent(
                multipart.getContentType(), multipart.getBytes());

        File uploaded = drive.files()
                .create(metadata, content)
                .setFields("id, name, mimeType, size, webViewLink, webContentLink, thumbnailLink")
                .execute();

        if (makePublic) {
            Permission p = new Permission()
                    .setType("anyone")
                    .setRole("reader");
            drive.permissions().create(uploaded.getId(), p).execute();
        }

        String fileId = uploaded.getId();

        // 우리가 지금 화면에서 쓰는 패턴
        String thumbnailUrl = "https://drive.google.com/thumbnail?id=" + fileId + "&sz=w1000";

        return new UploadedFileInfo(
                fileId,
                uploaded.getName(),
                uploaded.getMimeType(),
                uploaded.getSize() == null ? 0L : uploaded.getSize(),
                uploaded.getWebViewLink(),
                uploaded.getWebContentLink(),
                thumbnailUrl,          // ← 여기
                uploaded.getThumbnailLink()
        );
    }

    public void delete(String fileId) throws Exception {
        drive.files().delete(fileId).execute();
    }

    public record UploadedFileInfo(
            String fileId,
            String name,
            String mimeType,
            long size,
            String webViewLink,
            String webContentLink,
            String thumbnailUrl,   // 우리가 만든 썸네일 주소
            String googleThumbUrl  // 구글이 원래 주는 thumbnailLink (작을 수 있음)
    ) {}

    
}
