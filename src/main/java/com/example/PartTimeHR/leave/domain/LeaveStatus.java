package com.example.PartTimeHR.leave.domain;

public enum LeaveStatus {
    PENDING,   // 신청됨 (사장 승인 대기)
    APPROVED,  // 승인 (유급 처리, 결근 아님)
    REJECTED   // 거절
}
