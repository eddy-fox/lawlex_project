package com.soldesk.team_project.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LawyerDTO {

    private Integer lawyerIdx;
    private String  lawyerId;

    private String  lawyerPass;
    private String  lawyerName;
    private String  lawyerIdnum;
    private String  lawyerEmail;
    private String  lawyerPhone;
    private String  lawyerAgree;
    private String  lawyerNickname;

    private Integer lawyerAuth;        // 0:대기,1:승인,2:반려
    private String  lawyerAddress;
    private String  lawyerTel;

    private String  lawyerImgPath;     
    private String  lawyerComment;
    private Integer lawyerLike;
    private Integer lawyerAnswerCnt;   
    private Integer lawyerActive;
    private String lawyerProvider;
    private String lawyerProviderId;      

    private Integer interestIdx;
    private String  interestName;

    // 등록(업로드) 시 사용하는 필드가 있다면 함께 둠
    private Integer interestIdx1;
    private Integer interestIdx2;
    private Integer interestIdx3;

    // 파일 업로드 DTO로 쓰는 경우 (register에서 사용)
    private MultipartFile idImage;     // 신분증
    private MultipartFile certImage;   // 등록증
    private MultipartFile lawyerImage; // 변호사 사진
}
