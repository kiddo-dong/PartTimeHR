package com.example.PartTimeHR.analysis.service;

import com.example.PartTimeHR.analysis.domain.*;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class WorkAnalysisService {

    public List<DailyWorkStatus> analyze(
            LocalDate startDate,
            LocalDate endDate,
            List<Schedule> schedules,
            List<WorkRecord> workRecords
    ) {

        List<LocalDate> dates = startDate
                .datesUntil(endDate.plusDays(1))
                .toList();

        Map<LocalDate, Schedule> scheduleMap =
                schedules.stream()
                        .collect(Collectors.toMap(
                                Schedule::getWorkDate,
                                s -> s
                        ));

        Map<LocalDate, WorkRecord> recordMap =
                workRecords.stream()
                        .collect(Collectors.toMap(
                                WorkRecord::getWorkDate,
                                r -> r
                        ));

        List<DailyWorkStatus> result = new ArrayList<>();

        for (LocalDate date : dates) {
            Schedule schedule = scheduleMap.get(date);
            WorkRecord record = recordMap.get(date);

            boolean scheduled = schedule != null;
            boolean worked = record != null;

            AttendanceStatus attendanceStatus =
                    decideAttendanceStatus(scheduled, worked);

            int workedMinutes = worked ? record.getNetWorkedMinutes() : 0;

            EnumSet<WorkFlag> flags =
                    (schedule != null && record != null)
                            ? decideWorkFlags(schedule, record)
                            : EnumSet.noneOf(WorkFlag.class);

            result.add(
                    DailyWorkStatus.of(
                            date,
                            scheduled,
                            worked,
                            attendanceStatus,
                            workedMinutes,
                            flags
                    )
            );
        }

        return result;
    }

    private AttendanceStatus decideAttendanceStatus(
            boolean scheduled,
            boolean worked
    ) {
        if (scheduled && !worked) {
            return AttendanceStatus.ABSENT;
        }
        if (worked) {
            return AttendanceStatus.NORMAL;
        }
        return AttendanceStatus.NORMAL;
    }

    // === 핵심: 스케줄 대비 초과근무 판단 ===
    private EnumSet<WorkFlag> decideWorkFlags(
            Schedule schedule,
            WorkRecord record
    ) {
        EnumSet<WorkFlag> flags = EnumSet.noneOf(WorkFlag.class);

        int scheduledMinutes = calculateScheduledMinutes(schedule);
        int workedMinutes = record.getNetWorkedMinutes();

        if (workedMinutes > scheduledMinutes) {
            flags.add(WorkFlag.OVERTIME);
        }

        return flags;
    }

    // === Schedule 근무 예정 시간 계산 (엔티티 수정 ❌) ===
    private int calculateScheduledMinutes(Schedule schedule) {
        return (int) Duration.between(
                schedule.getStartTime(),
                schedule.getEndTime()
        ).toMinutes();
    }
}