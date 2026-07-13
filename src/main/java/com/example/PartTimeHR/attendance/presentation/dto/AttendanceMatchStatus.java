package com.example.PartTimeHR.attendance.presentation.dto;

public enum AttendanceMatchStatus {
    WORKED,       // 정상 근무
    LATE,         // 지각
    EARLY_LEAVE,  // 조퇴
    PARTIAL,      // 근무 중 (아직 퇴근 전)
    ABSENT,       // 결근 (스케줄 종료까지 출근 기록 없음)
    SCHEDULED,    // 근무 예정 (스케줄이 아직 끝나지 않음 - 결근 아님)
    ON_LEAVE,     // 승인된 연차 사용 (결근 아님)
    UNSCHEDULED   // 스케줄 없이 근무
}
