package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionDTO {
    
    private Integer qIdx;
    private String qTitle;
    private String qContent;
    private LocalDate qRegDate;
    private String qSecret;
    private Integer qAnswer;
    private Integer qActive;
    private Integer memberIdx;
    private Integer lawyerIdx;

}
