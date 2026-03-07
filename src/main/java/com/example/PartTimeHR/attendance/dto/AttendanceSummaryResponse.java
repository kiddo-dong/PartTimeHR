package com.example.PartTimeHR.attendance.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AttendanceSummaryResponse {
    private LocalDate from;
    private LocalDate to;

    private int scheduledCount;
    private int workedCount;
    private int absentCount;
    private int unscheduledCount;
    private int lateCount;

    private double attendanceRate;
}
