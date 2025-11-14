package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommentDTO {

    private Integer commentIdx;
    private String reboardContent;
    private LocalDate reboardRegDate;
    private Integer newsIdx;
    private Integer memberIdx;
    private Integer commentActive;

}
