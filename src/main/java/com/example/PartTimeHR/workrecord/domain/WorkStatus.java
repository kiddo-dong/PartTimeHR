package com.example.PartTimeHR.workrecord.domain;

public enum WorkStatus {
    IN_PROGRESS,  // 근무 중 (출근만 함)
    ON_BREAK,     // 휴게 중 (휴게 시작함)
    COMPLETED,      // 근무 완료 (퇴근함)
    ABSENT // 결근 여부
}

