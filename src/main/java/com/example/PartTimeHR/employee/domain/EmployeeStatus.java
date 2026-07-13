package com.example.PartTimeHR.employee.domain;

public enum EmployeeStatus {
    PENDING,  // 매장 초대코드로 가입, 사장 승인 대기 (로그인 불가)
    ACTIVE    // 사장이 직접 등록했거나 승인 완료 (로그인 가능)
}
