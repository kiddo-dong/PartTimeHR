package com.example.PartTimeHR.schedule.service;

import com.example.PartTimeHR.schedule.exception.DuplicateScheduleException;
import com.example.PartTimeHR.schedule.exception.InvalidScheduleException;
import com.example.PartTimeHR.schedule.repository.ScheduleRepository;
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

    public ScheduleRepository scheduleRepository;

    public void validateWorkTime(LocalTime startTime, LocalTime endTime) {

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
            LocalTime startTime,
            LocalTime endTime
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
}