package com.soldesk.team_project.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="calendar",
        indexes = {
            @Index(name="ix_cal_lawyer_dow", columnList = "lawyer_idx, calendar_weekname, calendar_active")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_idx")
    private Integer calendarIdx;

    @Column(name = "calendar_weekname")
    private Integer calendarWeekname;

    @Column(name = "calendar_starttime")
    private LocalTime calendarStartTime;

    @Column(name = "calendar_endtime")
    private LocalTime calendarEndTime;

    @Column(name = "calendar_active")
    private Integer calendarActive;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_idx")
    private LawyerEntity lawyer;
}
