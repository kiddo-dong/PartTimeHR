package com.example.PartTimeHR.workrecord.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 직원 조회 전용(본인 - login 시 발행한 jwt 필요)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employee/work-records")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeWorkRecordQueryController {
    // 직원 JWT(Role 및 Authentication)기반 GET 권한 처리하기
}