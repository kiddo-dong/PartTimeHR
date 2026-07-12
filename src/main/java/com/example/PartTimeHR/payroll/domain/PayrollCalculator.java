package com.example.PartTimeHR.payroll.domain;

import com.example.PartTimeHR.schedule.domain.ScheduleDateCalculator;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 급여 계산 (MVP 단순화 버전).
 *
 * - 기본급: 기록별 실근무 분 × 당시 시급 스냅샷 (원 단위 반올림)
 * - 주휴수당: 매장이 주휴 적용(weeklyPayApplicable)이고 한 주(매장의 주 시작
 *   요일 기준) 실근무가 15시간 이상이면
 *   min(주 실근무시간, 40) / 40 × 8시간 × 그 주 평균 시급
 *
 * 단순화한 부분 (실제 노무 기준과 다를 수 있음):
 * - 개근(소정근로일 만근) 여부는 확인하지 않는다
 * - 조회 구간이 주 중간을 자르면 그 주는 잘린 부분만으로 집계된다
 */
public class PayrollCalculator {

    private static final int WEEKLY_ALLOWANCE_MIN_MINUTES = 15 * 60;
    private static final double FULL_TIME_WEEK_HOURS = 40.0;
    private static final double WEEKLY_ALLOWANCE_HOURS = 8.0;

    public record Result(
            int totalNetMinutes,
            long basePay,
            long weeklyAllowance,
            long totalPay
    ) {}

    /** 기록 1건의 기본급 (실근무 분 × 시급 스냅샷) */
    public static long recordPay(WorkRecord record) {
        return Math.round(record.getActualWorkMinutes() * record.getAppliedHourlyWage() / 60.0);
    }

    /** 퇴근 완료된 기록들로 한 직원의 급여를 계산한다 */
    public static Result calculate(
            List<WorkRecord> completedRecords,
            int weekStartDay,
            boolean weeklyPayApplicable
    ) {
        int totalNetMinutes = 0;
        long basePay = 0;

        // 주(週)별 실근무 분/기본급 집계 (주휴수당 계산용)
        Map<LocalDate, Integer> weekMinutes = new HashMap<>();
        Map<LocalDate, Long> weekPay = new HashMap<>();

        for (WorkRecord record : completedRecords) {
            long pay = recordPay(record);

            totalNetMinutes += record.getActualWorkMinutes().intValue();
            basePay += pay;

            LocalDate weekStart = ScheduleDateCalculator.getWeekStartDate(record.getWorkDate(), weekStartDay);
            weekMinutes.merge(weekStart, record.getActualWorkMinutes().intValue(), Integer::sum);
            weekPay.merge(weekStart, pay, Long::sum);
        }

        long weeklyAllowance = 0;
        if (weeklyPayApplicable) {
            for (Map.Entry<LocalDate, Integer> week : weekMinutes.entrySet()) {
                int minutes = week.getValue();
                if (minutes < WEEKLY_ALLOWANCE_MIN_MINUTES) {
                    continue;
                }

                double hours = Math.min(minutes / 60.0, FULL_TIME_WEEK_HOURS);
                double averageWage = weekPay.get(week.getKey()) * 60.0 / minutes;

                weeklyAllowance += Math.round(
                        hours / FULL_TIME_WEEK_HOURS * WEEKLY_ALLOWANCE_HOURS * averageWage
                );
            }
        }

        return new Result(totalNetMinutes, basePay, weeklyAllowance, basePay + weeklyAllowance);
    }
}
