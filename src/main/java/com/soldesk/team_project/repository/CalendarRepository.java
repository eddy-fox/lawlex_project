package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.CalendarEntity;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Integer> {

    /** 특정 변호사 + 특정 요일의 활성 구간 (시작시간 오름차순) */
    List<CalendarEntity>
    findByLawyerLawyerIdxAndCalendarWeeknameAndCalendarActiveOrderByCalendarStartTimeAsc(
            Integer lawyerIdx, Integer calendarWeekname, Integer calendarActive);

    /** 특정 변호사의 전체 활성 구간 (요일/시작시간 오름차순) */
    List<CalendarEntity>
    findByLawyerLawyerIdxAndCalendarActiveOrderByCalendarWeeknameAscCalendarStartTimeAsc(
            Integer lawyerIdx, Integer calendarActive);

    /** 특정 요일의 전체 변호사 활성 구간 (시작시간 오름차순) */
    List<CalendarEntity>
    findByCalendarWeeknameAndCalendarActiveOrderByCalendarStartTimeAsc(
            Integer calendarWeekname, Integer calendarActive);

    /** 특정 변호사의 전체 구간(활성/비활성 포함) – 일괄 교체용 */
    List<CalendarEntity>
    findByLawyerLawyerIdxOrderByCalendarWeeknameAscCalendarStartTimeAsc(
            Integer lawyerIdx);
}
