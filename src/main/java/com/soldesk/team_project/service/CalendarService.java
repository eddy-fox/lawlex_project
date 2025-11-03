package com.soldesk.team_project.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.dto.CalendarDTO;
import com.soldesk.team_project.entity.CalendarEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.CalendarRepository;
import com.soldesk.team_project.repository.LawyerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final LawyerRepository   lawyerRepository;

    /* ================== 변환 유틸 (DTO <-> Entity) ================== */

    // Integer(분) → LocalTime
    private static LocalTime minutesToTime(Integer minutes) {
        if (minutes == null) return null;
        int h = minutes / 60;
        int m = minutes % 60;
        return LocalTime.of(h, m);
    }

    // LocalTime → Integer(분)
    private static Integer timeToMinutes(LocalTime t) {
        if (t == null) return null;
        return t.getHour() * 60 + t.getMinute();
    }

    // "HH:mm" → Integer(분)
    private static int hhmmToMinutes(String hhmm) {
        LocalTime t = LocalTime.parse(hhmm);
        return t.getHour() * 60 + t.getMinute();
    }

    private CalendarDTO toDTO(CalendarEntity e) {
        CalendarDTO d = new CalendarDTO();
        d.setCalendarIdx(e.getCalendarIdx());
        d.setCalendarWeekname(e.getCalendarWeekname()); // 0=Mon..6=Sun
        d.setCalendarStartTime(timeToMinutes(e.getCalendarStartTime()));
        d.setCalendarEndTime(timeToMinutes(e.getCalendarEndTime()));
        d.setCalendarActive(e.getCalendarActive());
        d.setLawyerIdx(e.getLawyer() != null ? e.getLawyer().getLawyerIdx() : null);
        return d;
    }

    /* ================== 기본 유틸/검증 ================== */

    private static boolean validRange(LocalTime s, LocalTime e) {
        return s != null && e != null && s.isBefore(e);
    }

    private static int todayDow0to6() {
        int v = LocalDate.now().getDayOfWeek().getValue(); // Mon=1..Sun=7
        return (v + 6) % 7; // 0..6
    }

    /* ================== 조회 메서드 ================== */

    /** 특정 변호사 + 특정 요일(활성) */
    @Transactional(readOnly = true)
    public List<CalendarDTO> findActiveSlots(Integer lawyerIdx, Integer weekday) {
        var list = calendarRepository
                .findByLawyerLawyerIdxAndCalendarWeeknameAndCalendarActiveOrderByCalendarStartTimeAsc(
                        lawyerIdx, weekday, 1);
        return list.stream().map(this::toDTO).toList();
    }

    /** 특정 변호사 전체 활성 (요일/시작시간 오름차순) */
    @Transactional(readOnly = true)
    public List<CalendarDTO> findAllActiveByLawyer(Integer lawyerIdx) {
        var list = calendarRepository
                .findByLawyerLawyerIdxAndCalendarActiveOrderByCalendarWeeknameAscCalendarStartTimeAsc(
                        lawyerIdx, 1);
        return list.stream().map(this::toDTO).toList();
    }

    /** 특정 요일 전체 변호사 활성 (시작시간 오름차순) */
    @Transactional(readOnly = true)
    public List<CalendarDTO> findAllActiveByWeekday(Integer weekday) {
        var list = calendarRepository
                .findByCalendarWeeknameAndCalendarActiveOrderByCalendarStartTimeAsc(weekday, 1);
        return list.stream().map(this::toDTO).toList();
    }

    /** 특정 변호사 전체(활성/비활성 포함) */
    @Transactional(readOnly = true)
    public List<CalendarDTO> findAllByLawyer(Integer lawyerIdx) {
        var list = calendarRepository
                .findByLawyerLawyerIdxOrderByCalendarWeeknameAscCalendarStartTimeAsc(lawyerIdx);
        return list.stream().map(this::toDTO).toList();
    }

    /* ================== 회원가입/수정 저장 ================== */

    /**
     * 회원가입: 체크한 요일들 각각에 동일한 시작/끝으로 1건씩 생성
     */
    @Transactional
    public void saveInitialAvailability(Integer lawyerIdx, List<Integer> weekdays,
                                        String startHHmm, String endHHmm) {
        var lawyer = lawyerRepository.findById(lawyerIdx).orElseThrow();

        int startMin = hhmmToMinutes(startHHmm);
        int endMin   = hhmmToMinutes(endHHmm);
        LocalTime s  = minutesToTime(startMin);
        LocalTime e  = minutesToTime(endMin);
        if (!validRange(s, e)) throw new IllegalArgumentException("시작/종료 시간이 올바르지 않습니다.");

        List<CalendarEntity> toSave = new ArrayList<>();
        for (Integer dow : weekdays) {
            if (dow == null || dow < 0 || dow > 6) continue; // 0..6
            CalendarEntity ce = new CalendarEntity();
            ce.setLawyer(lawyer);
            ce.setCalendarWeekname(dow);
            ce.setCalendarStartTime(s);
            ce.setCalendarEndTime(e);
            ce.setCalendarActive(1);
            toSave.add(ce);
        }
        toSave.sort(Comparator.comparing(CalendarEntity::getCalendarWeekname)
                              .thenComparing(CalendarEntity::getCalendarStartTime));
        calendarRepository.saveAll(toSave);
    }

    /**
     * 회원정보수정: 기존 전부 삭제 후 체크한 요일로 재생성(Replace-all)
     */
    @Transactional
    public void updateAvailabilityReplace(Integer lawyerIdx, List<Integer> weekdays,
                                          String startHHmm, String endHHmm) {
        var old = calendarRepository
                .findByLawyerLawyerIdxOrderByCalendarWeeknameAscCalendarStartTimeAsc(lawyerIdx);
        calendarRepository.deleteAllInBatch(old);
        saveInitialAvailability(lawyerIdx, weekdays, startHHmm, endHHmm);
    }

    /* ========== 지금 신청 가능? (마감 1시간 룰 포함) ========== */

    /**
     * 현재 시간 기준 신청 가능 여부
     * - 오늘 요일의 활성 구간 중 now ∈ [start, end)
     * - now + durationMinutes ≤ end
     * - end - 60분 이후 신청 불가
     */
    @Transactional(readOnly = true)
    public boolean canRequestNow(Integer lawyerIdx, Integer durationMinutes) {
        int dow = todayDow0to6();
        var list = calendarRepository
                .findByLawyerLawyerIdxAndCalendarWeeknameAndCalendarActiveOrderByCalendarStartTimeAsc(
                        lawyerIdx, dow, 1);
        if (list.isEmpty()) return false;

        LocalTime now = LocalTime.now();
        int dur = (durationMinutes == null || durationMinutes <= 0) ? 60 : durationMinutes;
        LocalTime willEnd = now.plusMinutes(dur);

        for (var c : list) {
            LocalTime start = c.getCalendarStartTime();
            LocalTime end   = c.getCalendarEndTime();
            if (now.isBefore(end) && !now.isBefore(start)) {
                LocalTime lastStartAllowed = end.minusMinutes(60);
                if (!willEnd.isAfter(end) && !now.isAfter(lastStartAllowed)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* ========== 멤버 메인용: 요일별 변호사 카드 데이터(Map) ========== */

    /**
     * 변호사 카드 리스트(뱃지/가능여부 없이 이름/시간만)
     * - 반환: [{lawyerIdx, lawyerName, weekday, start, end}, ...]
     * - 같은 변호사는 대표 구간으로 가장 이른 시작 시간을 사용
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listLawyersForDayAsMap(int weekday) {
        var slots = calendarRepository
                .findByCalendarWeeknameAndCalendarActiveOrderByCalendarStartTimeAsc(weekday, 1);
        if (slots.isEmpty()) return List.of();

        Map<LawyerEntity, List<CalendarEntity>> byLawyer =
                slots.stream().collect(Collectors.groupingBy(CalendarEntity::getLawyer));

        List<Map<String, Object>> list = new ArrayList<>();
        for (var entry : byLawyer.entrySet()) {
            var lawyer   = entry.getKey();
            var daySlots = entry.getValue();

            var first = daySlots.stream()
                    .min(Comparator.comparing(CalendarEntity::getCalendarStartTime))
                    .orElse(null);

            Map<String, Object> m = new HashMap<>();
            m.put("lawyerIdx",  lawyer.getLawyerIdx());
            m.put("lawyerName", lawyer.getLawyerName());
            m.put("weekday",    weekday);
            m.put("start",      first != null ? first.getCalendarStartTime().toString() : null);
            m.put("end",        first != null ? first.getCalendarEndTime().toString()   : null);
            list.add(m);
        }

        // 시작 시간이 빠른 순으로만 정렬
        list.sort(Comparator.comparing(m -> {
            var s = (String) m.get("start");
            return s == null ? "99:99" : s;
        }));
        return list;
    }
}
