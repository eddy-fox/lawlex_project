package com.soldesk.team_project.dto;


import lombok.*;
@Getter 
@Setter 
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 

public class MemberDTO {
    private Integer memberIdx;
    private String memberId;
    private String memberPass;
    private String memberPass2;
    private String memberName;
    private String memberIdnum;
    private String memberEmail;
    private String memberPhone;
    private String memberAgree;
    private String memberNickname;
    private Integer memberPoint;
    private Integer memberActive;
    private String interestName;
    private Integer interestIdx;
    public Integer interestIdx1; 
    public Integer interestIdx2; 
    public Integer interestIdx3;
 
}