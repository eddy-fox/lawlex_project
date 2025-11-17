package com.soldesk.team_project.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import com.soldesk.team_project.service.FirebaseStorageService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final FirebaseStorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") @NotBlank String folder
    ) {
        // 안전한 짧은 파일명 생성: 20251113_213015123-8chars.ext
        String ext = getExtension(file.getOriginalFilename());     // .jpg, .png ...
        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
        String safeName = now + "-" + shortUuid + ext;             // 예: 20251113_213015123-7f3a9c1b.jpg

        // 폴더 조합 (슬래시/역슬래시 제거)
        String safeFolder = folder.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
        String objectPath = safeFolder + "/" + safeName;

        var uploaded = storageService.upload(file, objectPath);
        return ResponseEntity.ok(Map.of(
                "fileId", uploaded.fileId(),   // 스토리지 경로
                "url", uploaded.url()          // 공개 URL
        ));
    }

    /** 원본 파일명에서 확장자만 추출 (.jpg 등). 없으면 .bin */
    private String getExtension(String original) {
        if (original == null || original.isBlank()) return ".bin";
        String name = original.trim();
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0 || lastDot == name.length() - 1) return ".bin";
        // 확장자 표준화 (소문자)
        String ext = name.substring(lastDot).toLowerCase(Locale.ROOT);
        // 너무 긴/이상한 확장자 방어
        if (ext.length() > 10 || ext.contains("/") || ext.contains("\\") || ext.contains(" ")) {
            return ".bin";
        }
        return ext;
    }
}
