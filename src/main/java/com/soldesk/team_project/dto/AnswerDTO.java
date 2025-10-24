package com.soldesk.team_project.dto;

import java.time.LocalDate;

import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.QuestionEntity;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerDTO {
    
    private LawyerEntity lawyerIdx;
    private String aContent;
    private LocalDate aRegDate;
    private QuestionEntity question;
    private LawyerEntity lawyerName;
    
}
