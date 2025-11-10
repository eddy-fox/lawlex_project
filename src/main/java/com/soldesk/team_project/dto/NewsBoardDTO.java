package com.soldesk.team_project.dto;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsBoardDTO {

    private Integer newsIdx;
    private String newsTitle;
    private String newsContent;
    private LocalDate newsRegDate;
    private String newsImgpPath;
    private Integer newsLike;
    private Integer newsViews;

    // 작성자 구분
    private Integer adminIdx;
    private Integer lawyerIdx;
    private Integer categoryIdx;

    // 상태값 (1: 노출, 0: 삭제)
    private Integer newsActive;

    // 동영상 전용
    private String videoUrl;

    // 파일 업로드
    private MultipartFile newsBoardFile;
    private String originalFileName;
    private String storedFileName;
    private int fileAttached;
    private String driveFileId;
}
