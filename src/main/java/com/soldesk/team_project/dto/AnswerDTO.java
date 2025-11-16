package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerDTO {
    
    private Integer aIdx;
    private String aContent;
    private LocalDate aRegDate;
    private Integer aActive;
    private Integer qIdx;
    private Integer adminIdx;
    
}
