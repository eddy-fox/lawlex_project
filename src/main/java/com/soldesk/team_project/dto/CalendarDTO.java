package com.soldesk.team_project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarDTO {
    
    private Integer calendarIdx;
    private Integer calendarWeekname;
    private Integer calendarStartTime;
    private Integer calendarEndTime;
    private Integer calendarActive;
    
    private Integer lawyerIdx;
}
