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

    /**
     * 회원정보수정: 여러 시간대를 한 번에 처리 (소프트 삭제 방식)
     * - 추가/수정: calendar_active = 1
     * - 삭제: calendar_active = 0
     * @param lawyerIdx 변호사 ID
     * @param timeSlots 시간대 리스트 (각 항목: {weekdays: [0,1,2], start: "09:00", end: "18:00"})
     */
    @Transactional
    public void updateAvailabilityMultiple(Integer lawyerIdx, List<Map<String, Object>> timeSlots) {
        var lawyer = lawyerRepository.findById(lawyerIdx).orElseThrow();
        
        // 기존 모든 calendar 데이터 가져오기 (active 포함)
        List<CalendarEntity> existing = calendarRepository
                .findByLawyerLawyerIdxOrderByCalendarWeeknameAscCalendarStartTimeAsc(lawyerIdx);
        
        // 새로운 timeSlots를 Set으로 변환하여 빠른 조회
        Map<String, CalendarEntity> newSlotsMap = new HashMap<>();
        if (timeSlots != null && !timeSlots.isEmpty()) {
            for (Map<String, Object> slot : timeSlots) {
                @SuppressWarnings("unchecked")
                List<Integer> weekdays = (List<Integer>) slot.get("weekdays");
                String startHHmm = (String) slot.get("start");
                String endHHmm = (String) slot.get("end");
                
                if (weekdays == null || weekdays.isEmpty() || startHHmm == null || endHHmm == null) {
                    continue;
                }
                
                int startMin = hhmmToMinutes(startHHmm);
                int endMin = hhmmToMinutes(endHHmm);
                LocalTime s = minutesToTime(startMin);
                LocalTime e = minutesToTime(endMin);
                
                if (!validRange(s, e)) {
                    continue;
                }
                
                for (Integer dow : weekdays) {
                    if (dow == null || dow < 0 || dow > 6) continue;
                    // 키: "요일_시작시간_끝시간" 형식
                    String key = dow + "_" + s.toString() + "_" + e.toString();
                    newSlotsMap.put(key, null); // 나중에 실제 Entity로 교체
                }
            }
        }
        
        // 기존 데이터 처리: 일치하면 active=1, 없으면 active=0
        List<CalendarEntity> toUpdate = new ArrayList<>();
        for (CalendarEntity existingEntity : existing) {
            String key = existingEntity.getCalendarWeekname() + "_" 
                        + existingEntity.getCalendarStartTime().toString() + "_" 
                        + existingEntity.getCalendarEndTime().toString();
            
            if (newSlotsMap.containsKey(key)) {
                // 새로운 timeSlots에 있으면 active=1
                existingEntity.setCalendarActive(1);
                toUpdate.add(existingEntity);
                newSlotsMap.put(key, existingEntity); // 기존 Entity 사용
            } else {
                // 새로운 timeSlots에 없으면 active=0 (소프트 삭제)
                existingEntity.setCalendarActive(0);
                toUpdate.add(existingEntity);
            }
        }
        
        // 새로운 시간대 추가 (기존에 없던 것만)
        List<CalendarEntity> toSave = new ArrayList<>();
        if (timeSlots != null && !timeSlots.isEmpty()) {
            for (Map<String, Object> slot : timeSlots) {
                @SuppressWarnings("unchecked")
                List<Integer> weekdays = (List<Integer>) slot.get("weekdays");
                String startHHmm = (String) slot.get("start");
                String endHHmm = (String) slot.get("end");
                
                if (weekdays == null || weekdays.isEmpty() || startHHmm == null || endHHmm == null) {
                    continue;
                }
                
                int startMin = hhmmToMinutes(startHHmm);
                int endMin = hhmmToMinutes(endHHmm);
                LocalTime s = minutesToTime(startMin);
                LocalTime e = minutesToTime(endMin);
                
                if (!validRange(s, e)) {
                    continue;
                }
                
                for (Integer dow : weekdays) {
                    if (dow == null || dow < 0 || dow > 6) continue;
                    String key = dow + "_" + s.toString() + "_" + e.toString();
                    
                    // 기존에 없던 것만 새로 생성
                    if (newSlotsMap.get(key) == null) {
                        CalendarEntity ce = new CalendarEntity();
                        ce.setLawyer(lawyer);
                        ce.setCalendarWeekname(dow);
                        ce.setCalendarStartTime(s);
                        ce.setCalendarEndTime(e);
                        ce.setCalendarActive(1);
                        toSave.add(ce);
                    }
                }
            }
        }
        
        // 업데이트 및 저장
        calendarRepository.saveAll(toUpdate);
        if (!toSave.isEmpty()) {
            calendarRepository.saveAll(toSave);
        }
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

        // null 체크
        if (start == null || end == null) {
            continue;
        }

        // 지금이 이 슬롯 안에 있어야 함
        if (now.isBefore(end) && !now.isBefore(start)) {

            // 60분 상담은 "끝나기 60분 전"까지만
            if (dur == 60) {
                LocalTime lastStart60 = end.minusMinutes(60);
                if (now.isAfter(lastStart60)) {
                    return false; // 60분은 불가
                }
                // 여기까지 왔으면 end까지 60분 남으니까 OK
                return true;
            }

            // 30분 상담은 end까지 30분만 남아도 OK
            if (dur == 30) {
                if (willEnd.isAfter(end)) {
                    return false; // 30분 넣었는데 end를 넘어버리면 불가
                }
                return true;
            }

            // 다른 시간대 쓰고 싶을 때 기본
            if (!willEnd.isAfter(end)) {
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
    int todayDow = todayDow0to6();
    var slots = calendarRepository
            .findByCalendarWeeknameAndCalendarActiveOrderByCalendarStartTimeAsc(weekday, 1);
    if (slots.isEmpty()) return List.of();

    Map<LawyerEntity, List<CalendarEntity>> byLawyer =
            slots.stream().collect(Collectors.groupingBy(CalendarEntity::getLawyer));

    List<Map<String, Object>> available = new ArrayList<>();
    List<Map<String, Object>> unavailable = new ArrayList<>();

    for (var entry : byLawyer.entrySet()) {
        LawyerEntity lawyer = entry.getKey();
        List<CalendarEntity> daySlots = entry.getValue();

        CalendarEntity first = daySlots.stream()
                .filter(s -> s.getCalendarStartTime() != null && s.getCalendarEndTime() != null)
                .min(Comparator.comparing(CalendarEntity::getCalendarStartTime))
                .orElse(null);

        Map<String, Object> m = new HashMap<>();
        m.put("lawyerIdx",  lawyer.getLawyerIdx());
        m.put("imgPath",    lawyer.getLawyerImgPath());
        m.put("name",       lawyer.getLawyerName());
        m.put("address",    lawyer.getLawyerAddress());
        m.put("interestName", lawyer.getInterest() != null ? lawyer.getInterest().getInterestName() : null);
        m.put("timeRange",  (first != null && first.getCalendarStartTime() != null && first.getCalendarEndTime() != null)
                ? first.getCalendarStartTime() + " ~ " + first.getCalendarEndTime()
                : "상시");

        boolean can30 = false;
        boolean can60 = false;

        if (weekday == todayDow) {
            can30 = canRequestNow(lawyer.getLawyerIdx(), 30);
            can60 = canRequestNow(lawyer.getLawyerIdx(), 60);
        }

        m.put("can30", can30);
        m.put("can60", can60);

        // 신청 버튼 전체 활성/비활성(위에 먼저, 아래에 나중)
        if (weekday == todayDow && (can30 || can60)) {
            m.put("canRequestNow", true);
            available.add(m);
        } else {
            m.put("canRequestNow", false);
            unavailable.add(m);
        }
    }

    // 정렬은 위에서 했던 것처럼...
    Comparator<Map<String, Object>> byTime =
            Comparator.comparing((Map<String, Object> x) -> {
                String tr = (String) x.get("timeRange");
                if (tr != null && tr.contains("~")) return tr.split("~")[0].trim();
                return tr != null ? tr : "99:99";
            });
    available.sort(byTime);
    unavailable.sort(byTime);

    List<Map<String, Object>> result = new ArrayList<>();
    result.addAll(available);
    result.addAll(unavailable);
    return result;
}

    /**
     * 메인페이지용: 오늘 지금 상담 가능한 변호사만 반환 (최대 5개)
     * - canRequestNow가 true인 변호사만 필터링
     * - 시작 시간 순으로 정렬
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAvailableLawyersNow() {
        int todayDow = todayDow0to6();
        var allLawyers = listLawyersForDayAsMap(todayDow);
        
        // canRequestNow가 true인 것만 필터링
        return allLawyers.stream()
                .filter(lawyer -> {
                    Boolean canRequestNow = (Boolean) lawyer.get("canRequestNow");
                    return canRequestNow != null && canRequestNow;
                })
                .limit(5) // 최대 5개
                .collect(Collectors.toList());
    }


}
