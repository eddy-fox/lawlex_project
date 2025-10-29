package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardDTO {
    
    private Integer boardIdx;
    private String boardTitle;
    private String boardContent;
    private LocalDate boardRegDate;
    private String imgPath;
    private LocalDate boardCaseDate;
    private Integer boardViews;
    private Integer memberIdx;
    private Integer interestIdx;

}
