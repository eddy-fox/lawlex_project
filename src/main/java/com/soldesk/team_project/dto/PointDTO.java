package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointDTO {
    
    private Integer pointIdx;
    private String pointDivision;
    private Integer pointState;
    private String pointHistory;
    private LocalDate pointRegDate;
    private Integer memberIdx;

}
