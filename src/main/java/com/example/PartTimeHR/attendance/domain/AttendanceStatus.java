package com.example.PartTimeHR.attendance.domain;

public enum AttendanceStatus {
    WORKED,      // 출근함
    ABSENT,      // 스케줄 있었는데 출근 안함
    UNSCHEDULED  // 스케줄 없는데 출근
}