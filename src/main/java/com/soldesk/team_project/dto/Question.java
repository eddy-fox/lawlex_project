package com.soldesk.team_project.dto;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Question {
    
    private Integer qIdx;
    private String qTitle;
    private String qContent;
    private Date qRegDate;
    private String qSecret;
    private String qAnswer;
    private Integer memberIdx;

}
