package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestionDTO {
    
    private Integer qIdx;
    private String qTitle;
    private String qContent;
    private LocalDate qRegDate;
    private Integer qSecret;
    private Integer qAnswer;
    private Integer qActive;
    private Integer memberIdx;
    private Integer lawyerIdx;

    private String infoId;
    private String infoName;
    private String memberId;
    private String lawyerId;

}
