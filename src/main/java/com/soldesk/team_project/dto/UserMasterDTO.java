package com.soldesk.team_project.dto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserMasterDTO {
    private Long userIdx;      // user_master.user_idx
    private String userId;     // user_master.user_id (로그인 아이디)
    private String role;       // "MEMBER" / "LAWYER" / "ADMIN"
    private Integer memberIdx; // 연결된 프로필 idx 
    private Integer lawyerIdx;
    private Integer adminIdx;
}
