package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@Builder
@NoArgsConstructor     
@AllArgsConstructor 
public class BoardDTO {
    
    private Integer boardIdx;
    private String boardTitle;
    private String boardContent;
    private LocalDate boardRegDate;
    private String imgPath;
    private LocalDate boardCaseDate;
    private Integer boardViews;
    private Integer memberIdx;
    private Integer interest;

    private String boardCategory;

}
