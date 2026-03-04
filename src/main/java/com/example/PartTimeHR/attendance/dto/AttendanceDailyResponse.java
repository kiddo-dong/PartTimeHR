package com.example.PartTimeHR.attendance.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class AttendanceDailyResponse {
    private LocalDate date;

    private int scheduledCount;
    private int workedCount;
    private int absentCount;
    private int unscheduledCount;
    private int lateCount;

    private List<AttendanceDailyEmployeeResponse> items;
}
