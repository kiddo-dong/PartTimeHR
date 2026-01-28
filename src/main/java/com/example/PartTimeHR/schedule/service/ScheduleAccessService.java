package com.example.PartTimeHR.schedule.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.exception.DuplicateScheduleException;
import com.example.PartTimeHR.schedule.exception.InvalidScheduleException;
import com.example.PartTimeHR.schedule.repository.ScheduleRepository;
import com.example.PartTimeHR.store.domain.Store;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// 예외 처리 Service Logic
@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleAccessService {

    private final ScheduleRepository scheduleRepository;


    public void validateWorkTime(LocalDateTime startTime, LocalDateTime endTime) {

        if (startTime == null || endTime == null) {
            throw new InvalidScheduleException("근무 시간이 비어있습니다.");
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidScheduleException("근무 시간이 올바르지 않습니다.");
        }
    }

    // 시간 겹침 검증
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

    public Schedule getScheduleOrThrow(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다.")
                );
    }
}