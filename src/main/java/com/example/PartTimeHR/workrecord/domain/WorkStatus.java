package com.example.PartTimeHR.workrecord.domain;

// 저장하지 않는 파생 상태 (WorkRecord.getStatus()에서 유도)
public enum WorkStatus {
    IN_PROGRESS,  // 근무 중 (출근만 함)
    ON_BREAK,     // 휴게 중 (열린 휴게 존재)
    COMPLETED     // 근무 완료 (퇴근함)
}
