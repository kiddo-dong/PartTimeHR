package com.example.PartTimeHR.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatisticsResponse {
    // 오늘 통계
    private Long todayClockInCount;           // 오늘 출근한 직원 수
    private Long todayNotClockOutCount;       // 오늘 미퇴근 직원 수
    private Double todayTotalWorkHours;       // 오늘 총 근무 시간
    private Double todayTotalActualWorkHours; // 오늘 총 실제 근무 시간

    // 이번 달 통계
    private Double thisMonthTotalWorkHours;   // 이번 달 총 근무 시간
    private Double thisMonthTotalActualWorkHours; // 이번 달 총 실제 근무 시간
    private Long thisMonthWorkDays;           // 이번 달 근무 일수

    // 전체 직원 수
    private Long totalEmployeeCount;          // 전체 직원 수

    // 오늘 출근한 직원 목록 (간단 정보)
    private List<TodayEmployeeInfo> todayEmployees;
}

