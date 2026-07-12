package com.example.PartTimeHR.payroll.domain;

import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.domain.ScheduleDateCalculator;
import com.example.PartTimeHR.workrecord.domain.WorkBreak;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 급여 계산 - 근로기준법 기준.
 *
 * 1. 기본급: 기록별 실근무 분 × 당시 시급 스냅샷 (원 단위 반올림)
 *
 * 2. 주휴수당 (제55조, 사업장 규모 무관 적용):
 *    한 주(매장의 주 시작 요일 기준) 실근무 15시간 이상 + 개근이면
 *    min(주 실근무시간, 40) / 40 × 8시간 × 그 주 평균 시급
 *    - 개근: 그 주에 스케줄(소정근로일)이 있는 날마다 근무 기록이 존재
 *    - 스케줄을 쓰지 않는 매장(그 주 스케줄 0건)은 시간 기준만 적용
 *
 * 3. 연장근로 가산 (제56조, 상시 5인 이상 사업장만):
 *    1일 8시간 초과분 + 주 40시간 초과 중 일별로 잡히지 않은 추가분 × 시급 × 50%
 *
 * 4. 야간근로 가산 (제56조, 상시 5인 이상 사업장만):
 *    22:00~06:00 사이의 실근로 분 × 시급 × 50% (휴게 구간 제외)
 *
 * 단순화한 부분:
 * - 조회 구간이 주 중간을 자르면 그 주는 잘린 부분만으로 집계된다
 *   (정확한 정산은 주 시작 요일에 맞춘 구간으로 조회할 것)
 * - 휴일근로 가산은 미구현 (공휴일/약정휴일 정보 없음)
 */
public class PayrollCalculator {

    private static final int WEEKLY_ALLOWANCE_MIN_MINUTES = 15 * 60;
    private static final int DAILY_STANDARD_MINUTES = 8 * 60;
    private static final int WEEKLY_STANDARD_MINUTES = 40 * 60;
    private static final double FULL_TIME_WEEK_HOURS = 40.0;
    private static final double WEEKLY_ALLOWANCE_HOURS = 8.0;
    private static final double PREMIUM_RATE = 0.5;

    public record Result(
            int totalNetMinutes,
            long basePay,
            long weeklyAllowance,
            long overtimeAllowance,
            long nightAllowance,
            long totalPay
    ) {}

    /** 기록 1건의 기본급 (실근무 분 × 시급 스냅샷) */
    public static long recordPay(WorkRecord record) {
        return Math.round(record.getActualWorkMinutes() * record.getAppliedHourlyWage() / 60.0);
    }

    /** 퇴근 완료된 기록들과 스케줄(개근 판정용)로 한 직원의 급여를 계산한다 */
    public static Result calculate(
            List<WorkRecord> completedRecords,
            List<Schedule> schedules,
            int weekStartDay,
            boolean weeklyPayApplicable,
            boolean fiveOrMoreEmployees
    ) {
        int totalNetMinutes = 0;
        long basePay = 0;

        // 일/주 단위 집계
        Map<LocalDate, Integer> dayMinutes = new HashMap<>();
        Map<LocalDate, Long> dayPay = new HashMap<>();
        Map<LocalDate, Integer> weekMinutes = new HashMap<>();
        Map<LocalDate, Long> weekPay = new HashMap<>();
        Set<LocalDate> workedDays = new HashSet<>();

        long nightAllowance = 0;

        for (WorkRecord record : completedRecords) {
            int netMinutes = record.getActualWorkMinutes().intValue();
            long pay = recordPay(record);

            totalNetMinutes += netMinutes;
            basePay += pay;
            workedDays.add(record.getWorkDate());

            dayMinutes.merge(record.getWorkDate(), netMinutes, Integer::sum);
            dayPay.merge(record.getWorkDate(), pay, Long::sum);

            LocalDate weekStart = ScheduleDateCalculator.getWeekStartDate(record.getWorkDate(), weekStartDay);
            weekMinutes.merge(weekStart, netMinutes, Integer::sum);
            weekPay.merge(weekStart, pay, Long::sum);

            // 야간 가산 (5인 이상 사업장만)
            if (fiveOrMoreEmployees) {
                long nightMinutes = nightMinutes(record);
                nightAllowance += Math.round(
                        nightMinutes / 60.0 * record.getAppliedHourlyWage() * PREMIUM_RATE
                );
            }
        }

        // 주별 소정근로일 (스케줄이 있는 날짜) - 개근 판정용
        Map<LocalDate, Set<LocalDate>> scheduledDaysByWeek = new HashMap<>();
        for (Schedule schedule : schedules) {
            LocalDate weekStart = ScheduleDateCalculator.getWeekStartDate(schedule.getWorkDate(), weekStartDay);
            scheduledDaysByWeek
                    .computeIfAbsent(weekStart, key -> new HashSet<>())
                    .add(schedule.getWorkDate());
        }

        // 주휴수당
        long weeklyAllowance = 0;
        if (weeklyPayApplicable) {
            for (Map.Entry<LocalDate, Integer> week : weekMinutes.entrySet()) {
                int minutes = week.getValue();
                if (minutes < WEEKLY_ALLOWANCE_MIN_MINUTES) {
                    continue;
                }

                // 개근 판정: 스케줄 있는 날마다 근무 기록이 있어야 한다
                Set<LocalDate> scheduledDays = scheduledDaysByWeek.getOrDefault(week.getKey(), Set.of());
                boolean perfectAttendance = workedDays.containsAll(scheduledDays);
                if (!perfectAttendance) {
                    continue;
                }

                double hours = Math.min(minutes / 60.0, FULL_TIME_WEEK_HOURS);
                double averageWage = weekPay.get(week.getKey()) * 60.0 / minutes;

                weeklyAllowance += Math.round(
                        hours / FULL_TIME_WEEK_HOURS * WEEKLY_ALLOWANCE_HOURS * averageWage
                );
            }
        }

        // 연장근로 가산 (5인 이상 사업장만)
        long overtimeAllowance = 0;
        if (fiveOrMoreEmployees) {
            // 1일 8시간 초과분
            Map<LocalDate, Integer> dailyOvertimeByWeek = new HashMap<>();
            for (Map.Entry<LocalDate, Integer> day : dayMinutes.entrySet()) {
                int overtime = Math.max(0, day.getValue() - DAILY_STANDARD_MINUTES);
                if (overtime == 0) {
                    continue;
                }

                double averageWage = dayPay.get(day.getKey()) * 60.0 / day.getValue();
                overtimeAllowance += Math.round(overtime / 60.0 * averageWage * PREMIUM_RATE);

                LocalDate weekStart = ScheduleDateCalculator.getWeekStartDate(day.getKey(), weekStartDay);
                dailyOvertimeByWeek.merge(weekStart, overtime, Integer::sum);
            }

            // 주 40시간 초과 중 일별로 잡히지 않은 추가분 (중복 가산 방지)
            for (Map.Entry<LocalDate, Integer> week : weekMinutes.entrySet()) {
                int alreadyCounted = dailyOvertimeByWeek.getOrDefault(week.getKey(), 0);
                int weeklyOvertime = Math.max(0, week.getValue() - WEEKLY_STANDARD_MINUTES - alreadyCounted);
                if (weeklyOvertime == 0) {
                    continue;
                }

                double averageWage = weekPay.get(week.getKey()) * 60.0 / week.getValue();
                overtimeAllowance += Math.round(weeklyOvertime / 60.0 * averageWage * PREMIUM_RATE);
            }
        }

        long totalPay = basePay + weeklyAllowance + overtimeAllowance + nightAllowance;

        return new Result(totalNetMinutes, basePay, weeklyAllowance, overtimeAllowance, nightAllowance, totalPay);
    }

    /** 기록의 야간(22:00~06:00) 실근로 분 - 휴게 구간 제외 */
    static long nightMinutes(WorkRecord record) {
        List<LocalDateTime[]> workIntervals = workIntervals(record);

        long minutes = 0;
        for (LocalDateTime[] interval : workIntervals) {
            // 근무가 걸칠 수 있는 모든 야간 창(전날 22시 ~ 당일 06시)을 순회
            LocalDate day = interval[0].toLocalDate().minusDays(1);
            LocalDate lastDay = interval[1].toLocalDate();

            while (!day.isAfter(lastDay)) {
                LocalDateTime nightStart = day.atTime(22, 0);
                LocalDateTime nightEnd = day.plusDays(1).atTime(6, 0);

                minutes += overlapMinutes(interval[0], interval[1], nightStart, nightEnd);
                day = day.plusDays(1);
            }
        }
        return minutes;
    }

    /** 근무 구간에서 종료된 휴게 구간을 뺀 실근로 구간들 */
    private static List<LocalDateTime[]> workIntervals(WorkRecord record) {
        List<LocalDateTime[]> intervals = new ArrayList<>();
        intervals.add(new LocalDateTime[]{record.getClockInTime(), record.getClockOutTime()});

        for (WorkBreak b : record.getBreaks()) {
            if (b.isOpen()) {
                continue;
            }

            List<LocalDateTime[]> next = new ArrayList<>();
            for (LocalDateTime[] interval : intervals) {
                // 휴게 앞쪽 조각
                if (interval[0].isBefore(b.getStartTime())) {
                    next.add(new LocalDateTime[]{
                            interval[0],
                            min(interval[1], b.getStartTime())
                    });
                }
                // 휴게 뒤쪽 조각
                if (interval[1].isAfter(b.getEndTime())) {
                    next.add(new LocalDateTime[]{
                            max(interval[0], b.getEndTime()),
                            interval[1]
                    });
                }
            }
            intervals = next;
        }
        return intervals;
    }

    private static long overlapMinutes(
            LocalDateTime start1, LocalDateTime end1,
            LocalDateTime start2, LocalDateTime end2
    ) {
        LocalDateTime start = max(start1, start2);
        LocalDateTime end = min(end1, end2);
        if (!start.isBefore(end)) {
            return 0;
        }
        return Duration.between(start, end).toMinutes();
    }

    private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }
}
