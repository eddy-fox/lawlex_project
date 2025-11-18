package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReboardDTO {
    
    private Integer reboardIdx;
    private String reboardTitle;
    private String reboardContent;
    private LocalDate reboardRegDate;
    private Integer boardIdx;
    private Integer lawyerIdx;
    private Integer reboardActive;
    
}
