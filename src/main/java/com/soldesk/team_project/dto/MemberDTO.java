package com.soldesk.team_project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
    private Integer memberActive;
    private Integer interestIdx;

}