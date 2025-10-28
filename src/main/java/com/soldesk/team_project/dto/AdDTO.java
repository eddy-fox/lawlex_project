package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdDTO {

    private Integer adIdx;
    private String adName;
    private String adImgPath;
    private String adLink;
    private LocalDate adStartDate;
    private Integer adDuration;
    // private Integer adCost;
    private Integer adViews;
    private Integer adActive;
    private Integer lawyerIdx;

}
