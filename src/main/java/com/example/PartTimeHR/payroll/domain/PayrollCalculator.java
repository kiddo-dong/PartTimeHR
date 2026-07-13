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
 * 2. 주휴수당 (제55조, 사업장 규모 무관·지급 의무):
 *    한 주(매장의 주 시작 요일 기준) 실근무 15시간 이상 + 개근이면
 *    min(주 실근무시간, 40) / 40 × 8시간 × 그 주 평균 시급
 *    - 개근: 그 주에 스케줄(소정근로일)이 있는 날마다 근무 기록이 존재
 *    - 스케줄을 쓰지 않는 매장(그 주 스케줄 0건)은 시간 기준만 적용
 *    - 단, "주휴 포함 시급" 계약 매장(weeklyAllowanceIncluded)은 시급에
 *      이미 반영돼 있으므로 별도 계산하지 않는다 (지급 여부의 선택이 아님)
 *
 * 3. 연장근로 가산 (제56조, 상시 5인 이상 사업장만):
 *    1일 8시간 초과분 + 주 40시간 초과 중 일별로 잡히지 않은 추가분 × 시급 × 50%
 *
 * 4. 야간근로 가산 (제56조, 상시 5인 이상 사업장만):
 *    22:00~06:00 사이의 실근로 분 × 시급 × 50% (휴게 구간 제외)
 *
 * 5. 휴일근로 가산 (제56조 2항, 상시 5인 이상 사업장만):
 *    법정 유급휴일(관공서 공휴일·대체공휴일·근로자의 날)의 근로에
 *    8시간 이내 50%, 8시간 초과분 100% 가산
 *    - 휴일 근로일은 연장근로 계산에서 제외 (중복 가산 방지)
 *    - 공휴일에 스케줄이 있었지만 쉰 것은 결근이 아니므로 개근 판정에서 제외
 *
 * 단순화한 부분:
 * - 조회 구간이 주 중간을 자르면 그 주는 잘린 부분만으로 집계된다
 *   (정확한 정산은 주 시작 요일에 맞춘 구간으로 조회할 것)
 * - 휴일 판정은 기록의 workDate 기준 (자정 넘김 근무의 익일 유급휴일 분리 없음)
 * - 직원별 약정 주휴일(요일) 근무의 휴일 가산은 미구현 (주휴일 지정 데이터 없음)
 * - 유급휴일에 쉰 날의 유급휴일수당(미근무 유급분)은 미구현
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
            long holidayAllowance,
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
            boolean weeklyAllowanceIncluded,
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
        // 유급휴일에 쉰 것은 결근이 아니므로 소정근로일에서 제외
        Map<LocalDate, Set<LocalDate>> scheduledDaysByWeek = new HashMap<>();
        for (Schedule schedule : schedules) {
            if (KoreanHolidayCalendar.isPaidHoliday(schedule.getWorkDate(), fiveOrMoreEmployees)) {
                continue;
            }
            LocalDate weekStart = ScheduleDateCalculator.getWeekStartDate(schedule.getWorkDate(), weekStartDay);
            scheduledDaysByWeek
                    .computeIfAbsent(weekStart, key -> new HashSet<>())
                    .add(schedule.getWorkDate());
        }

        // 휴일근로 가산 (제56조 2항, 5인 이상만): 8시간 이내 50%, 초과분 100%
        long holidayAllowance = 0;
        Set<LocalDate> premiumHolidays = new HashSet<>();
        Map<LocalDate, Integer> holidayMinutesByWeek = new HashMap<>();
        if (fiveOrMoreEmployees) {
            for (Map.Entry<LocalDate, Integer> day : dayMinutes.entrySet()) {
                if (!KoreanHolidayCalendar.isPaidHoliday(day.getKey(), true)) {
                    continue;
                }
                premiumHolidays.add(day.getKey());

                int minutes = day.getValue();
                double averageWage = dayPay.get(day.getKey()) * 60.0 / minutes;

                int within = Math.min(minutes, DAILY_STANDARD_MINUTES);
                int over = Math.max(0, minutes - DAILY_STANDARD_MINUTES);

                holidayAllowance += Math.round(within / 60.0 * averageWage * PREMIUM_RATE);
                holidayAllowance += Math.round(over / 60.0 * averageWage); // 초과분은 100% 가산

                LocalDate weekStart = ScheduleDateCalculator.getWeekStartDate(day.getKey(), weekStartDay);
                holidayMinutesByWeek.merge(weekStart, minutes, Integer::sum);
            }
        }

        // 주휴수당 (시급 포함 계약이 아니면 별도 계산 - 법정 기본)
        long weeklyAllowance = 0;
        if (!weeklyAllowanceIncluded) {
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
            // 1일 8시간 초과분 (휴일 근로일은 휴일 가산에서 처리하므로 제외)
            Map<LocalDate, Integer> dailyOvertimeByWeek = new HashMap<>();
            for (Map.Entry<LocalDate, Integer> day : dayMinutes.entrySet()) {
                if (premiumHolidays.contains(day.getKey())) {
                    continue;
                }
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
            // 휴일 근로 분은 휴일 가산에서 처리했으므로 주 기준에서도 제외
            for (Map.Entry<LocalDate, Integer> week : weekMinutes.entrySet()) {
                int weekBase = week.getValue() - holidayMinutesByWeek.getOrDefault(week.getKey(), 0);
                int alreadyCounted = dailyOvertimeByWeek.getOrDefault(week.getKey(), 0);
                int weeklyOvertime = Math.max(0, weekBase - WEEKLY_STANDARD_MINUTES - alreadyCounted);
                if (weeklyOvertime == 0) {
                    continue;
                }

                double averageWage = weekPay.get(week.getKey()) * 60.0 / week.getValue();
                overtimeAllowance += Math.round(weeklyOvertime / 60.0 * averageWage * PREMIUM_RATE);
            }
        }

        long totalPay = basePay + weeklyAllowance + overtimeAllowance + nightAllowance + holidayAllowance;

        return new Result(totalNetMinutes, basePay, weeklyAllowance,
                overtimeAllowance, nightAllowance, holidayAllowance, totalPay);
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
