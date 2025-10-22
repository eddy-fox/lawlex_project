package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsBoardDTO {
    
    private Integer newsIdx;
    private String newsTitle;
    private String newsContent;
    private LocalDate newsRegDate;
    private String newsImgPath;
    private Integer newsLike;
    private Integer newsViews;
    
    private Integer adminIdx;
    private Integer lawyerIdx;
    private Integer categoryIdx; 
}
