package com.example.PartTimeHR.attendance.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AttendanceDailyEmployeeResponse {
    private Long employeeId;
    private String employeeName;
    private AttendanceMatchStatus status;

    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;

    private LocalDateTime actualClockInTime;
    private LocalDateTime actualClockOutTime;

    private int lateMinutes;
    private int earlyLeaveMinutes;
    private int workedMinutes;
}
