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
    private String qAnswer;
    private Integer memberIdx;

    private String memberId;

}
