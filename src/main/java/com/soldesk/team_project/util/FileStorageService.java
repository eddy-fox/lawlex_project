package com.soldesk.team_project.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    // 업로드 루트 경로 (설정값 없으면 기본: src/main/resources/static/uploads)
    @Value("${file.upload.root:src/main/resources/static/uploads}")
    private String root;

    /**
     * 변호사 관련 업로드 파일을 저장하고, 웹에서 접근 가능한 URL 경로를 반환한다.
     * 예외 발생 시 IOException을 던진다.
     */
    public String saveLawyerFile(MultipartFile file) throws IOException {
        // 1) 파일이 없거나 비었으면 업로드 진행하지 않고 null 반환
        if (file == null || file.isEmpty()) return null;

        // 2) 저장 폴더 경로: [루트]/lawyer  (없으면 생성)
        Path dir = Paths.get(root, "lawyer");
        Files.createDirectories(dir);

        // 3) 원본 파일명에서 확장자 추출 (없으면 빈 문자열)
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }

        // 4) 저장할 파일명 생성: [yyyyMMddHHmmss]_[UUID].[ext]
        String name = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + "_" + UUID.randomUUID() + ext;

        // 5) 실제 저장 경로 생성 후 파일 저장
        Path dest = dir.resolve(name);
        file.transferTo(dest.toFile());

        // 6) 웹에서 사용할 접근 경로 문자열 반환 (리소스 매핑 전제: /uploads/**)
        return "/uploads/lawyer/" + name;
    }
}
