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

        private static String convertFileName(MultipartFile multipart){
                String originalFileName = multipart.getOriginalFilename();
                if(originalFileName == null || originalFileName.isBlank()){
                        originalFileName = "upload";
                }

                String ext = ""; 
                int dot = originalFileName.lastIndexOf('.'); 
                if (dot != -1 && dot < originalFileName.length() - 1) { 
                        ext = originalFileName.substring(dot);
                } // ".png" 
                originalFileName = originalFileName.substring(0, dot); // 확장자 제외한 이름 

                String base = originalFileName.replaceAll("[\\\\/:*?\"<>|]", "_") // Windows 금지 문자 치환 
                        .replaceAll("\\s+", "_") // 공백 -> _ 
                        .trim(); 
                if (base.isEmpty()) base = "upload";
                String storedFileName = System.currentTimeMillis() + "_" + base;

                return storedFileName + ext;
        }


    // 폴더 지정 업로드
    public UploadedFileInfo upload(MultipartFile multipart, String parentFolderId) throws Exception {
        // 메타데이터
        File metadata = new File();
        metadata.setName(convertFileName(multipart));
        metadata.setParents(java.util.List.of(parentFolderId));

        // 파일 본문
        ByteArrayContent content = new ByteArrayContent(
                multipart.getContentType(), multipart.getBytes());

        File uploaded = drive.files()
                .create(metadata, content)
                .setFields("id, name, mimeType, size, webViewLink, webContentLink, thumbnailLink")
                .execute();

        // 공개 접근(링크만 알면 읽기) 원하면 설정
        if (makePublic) {
            Permission p = new Permission()
                    .setType("anyone")
                    .setRole("reader");
            drive.permissions().create(uploaded.getId(), p).execute();
        }

        // 직링크(간단): https://drive.google.com/uc?id=FILE_ID
        String directUrl = "https://drive.google.com/uc?id=" + uploaded.getId();

        return new UploadedFileInfo(
                uploaded.getId(),
                uploaded.getName(),
                uploaded.getMimeType(),
                uploaded.getSize() == null ? 0L : uploaded.getSize(),
                uploaded.getWebViewLink(),
                uploaded.getWebContentLink(),
                directUrl,
                uploaded.getThumbnailLink()
        );
    }

    public void delete(String fileId) throws Exception {
        drive.files().delete(fileId).execute();
    }

    public record UploadedFileInfo(
            String fileId, String name, String mimeType, long size,
            String webViewLink, String webContentLink, String directUrl, String thumbnailLink
    ) {}
    
}
