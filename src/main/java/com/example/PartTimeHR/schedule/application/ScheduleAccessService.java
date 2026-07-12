package com.example.PartTimeHR.schedule.application;

import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.domain.DuplicateScheduleException;
import com.example.PartTimeHR.schedule.domain.InvalidScheduleException;
import com.example.PartTimeHR.schedule.domain.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 스케줄 검증/조회 Service Logic
@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleAccessService {

    private final ScheduleRepository scheduleRepository;

    /**
     * 근무 시간 검증.
     * - 시작 < 종료
     * - 시작 시간의 날짜는 workDate와 일치 (불일치하면 겹침 검사가 무력화됨)
     * - 종료는 당일 또는 다음날까지 허용 (야간 근무)
     */
    public void validateWorkTime(LocalDate workDate, LocalDateTime startTime, LocalDateTime endTime) {

        if (startTime == null || endTime == null) {
            throw new InvalidScheduleException("근무 시간이 비어있습니다.");
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidScheduleException("근무 시간이 올바르지 않습니다.");
        }

        if (!startTime.toLocalDate().equals(workDate)) {
            throw new InvalidScheduleException("근무 시작 시간은 근무 날짜와 같은 날이어야 합니다.");
        }

        if (endTime.toLocalDate().isAfter(workDate.plusDays(1))) {
            throw new InvalidScheduleException("근무 종료는 다음날까지만 가능합니다.");
        }
    }

    // 시간 겹침 검증 (생성용)
    public void validateNoOverlap(
            Long employeeId,
            LocalDate workDate,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (scheduleRepository.existsOverlappingSchedule(
                employeeId,
                workDate,
                startTime,
                endTime
        )) {
            throw new DuplicateScheduleException();
        }
    }

    // 시간 겹침 검증 (수정용 - 수정 대상 자신은 제외)
    public void validateNoOverlap(
            Long employeeId,
            LocalDate workDate,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long excludeScheduleId
    ) {
        if (scheduleRepository.existsOverlappingScheduleExcluding(
                employeeId,
                workDate,
                startTime,
                endTime,
                excludeScheduleId
        )) {
            throw new DuplicateScheduleException();
        }
    }

    public Schedule getScheduleOrThrow(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다.")
                );
    }
}
