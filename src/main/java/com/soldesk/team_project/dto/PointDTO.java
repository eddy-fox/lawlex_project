package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointDTO {
    
    private Integer pointIdx;
    private Integer pointBalance;
    private String pointState;
    private LocalDate pointRegDate;
    private Integer memberIdx;
    private Integer lawyerIdx;

}
