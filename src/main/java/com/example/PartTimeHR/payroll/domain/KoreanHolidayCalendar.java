package com.example.PartTimeHR.payroll.domain;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Map;
import java.util.Set;

/**
 * 한국 법정 유급휴일 캘린더.
 *
 * - 관공서 공휴일 (관공서의 공휴일에 관한 규정): 2022년부터 상시 5인 이상
 *   사업장의 법정 유급휴일 (근로기준법 제55조 2항)
 * - 근로자의 날 (5/1, 근로자의날 제정에 관한 법률): 사업장 규모 무관 유급휴일
 *
 * ⚠ 유지보수 의무:
 * - 음력 기반 공휴일(설날·추석·부처님오신날)과 대체공휴일은 연도별 하드코딩.
 *   매년 정부 발표(과학기술정보통신부 월력요항)를 확인해 다음 해 데이터를 추가할 것
 * - 선거일·임시공휴일은 예측 불가 → 지정 발표 시 수동 추가 필요
 * - 데이터가 없는 연도는 고정(양력) 공휴일만 인식한다
 */
public final class KoreanHolidayCalendar {

    private KoreanHolidayCalendar() {
    }

    // 매년 고정인 양력 공휴일 (연도 데이터가 없어도 인식)
    private static final Set<MonthDay> FIXED_SOLAR_HOLIDAYS = Set.of(
            MonthDay.of(1, 1),    // 신정
            MonthDay.of(3, 1),    // 삼일절
            MonthDay.of(5, 5),    // 어린이날
            MonthDay.of(6, 6),    // 현충일
            MonthDay.of(8, 15),   // 광복절
            MonthDay.of(10, 3),   // 개천절
            MonthDay.of(10, 9),   // 한글날
            MonthDay.of(12, 25)   // 성탄절
    );

    // 연도별 변동 공휴일 (음력 명절 + 대체공휴일 + 선거일·임시공휴일)
    private static final Map<Integer, Set<LocalDate>> VARIABLE_HOLIDAYS = Map.of(
            2025, Set.of(
                    LocalDate.of(2025, 1, 27),   // 임시공휴일 (설 연휴 연결)
                    LocalDate.of(2025, 1, 28),   // 설 연휴
                    LocalDate.of(2025, 1, 29),   // 설날
                    LocalDate.of(2025, 1, 30),   // 설 연휴
                    LocalDate.of(2025, 3, 3),    // 삼일절 대체공휴일
                    LocalDate.of(2025, 5, 6),    // 어린이날·부처님오신날 대체공휴일
                    LocalDate.of(2025, 6, 3),    // 대통령선거일 (임시공휴일)
                    LocalDate.of(2025, 10, 5),   // 추석 연휴
                    LocalDate.of(2025, 10, 6),   // 추석
                    LocalDate.of(2025, 10, 7),   // 추석 연휴
                    LocalDate.of(2025, 10, 8)    // 추석 대체공휴일
            ),
            2026, Set.of(
                    LocalDate.of(2026, 2, 16),   // 설 연휴
                    LocalDate.of(2026, 2, 17),   // 설날
                    LocalDate.of(2026, 2, 18),   // 설 연휴
                    LocalDate.of(2026, 3, 2),    // 삼일절 대체공휴일
                    LocalDate.of(2026, 5, 24),   // 부처님오신날
                    LocalDate.of(2026, 5, 25),   // 부처님오신날 대체공휴일
                    LocalDate.of(2026, 6, 3),    // 전국동시지방선거일
                    LocalDate.of(2026, 8, 17),   // 광복절 대체공휴일
                    LocalDate.of(2026, 9, 24),   // 추석 연휴
                    LocalDate.of(2026, 9, 25),   // 추석
                    LocalDate.of(2026, 9, 26),   // 추석 연휴
                    LocalDate.of(2026, 10, 5)    // 개천절 대체공휴일
            ),
            // 2027년은 월력요항 확정 발표 후 반드시 재검증할 것
            2027, Set.of(
                    LocalDate.of(2027, 2, 6),    // 설 연휴
                    LocalDate.of(2027, 2, 7),    // 설날
                    LocalDate.of(2027, 2, 8),    // 설 연휴
                    LocalDate.of(2027, 2, 9),    // 설날 대체공휴일
                    LocalDate.of(2027, 5, 13),   // 부처님오신날
                    LocalDate.of(2027, 8, 16),   // 광복절 대체공휴일
                    LocalDate.of(2027, 9, 14),   // 추석 연휴
                    LocalDate.of(2027, 9, 15),   // 추석
                    LocalDate.of(2027, 9, 16),   // 추석 연휴
                    LocalDate.of(2027, 10, 4),   // 개천절 대체공휴일
                    LocalDate.of(2027, 10, 11),  // 한글날 대체공휴일
                    LocalDate.of(2027, 12, 27)   // 성탄절 대체공휴일
            )
    );

    /** 관공서 공휴일인가 (5인 이상 사업장의 법정 유급휴일) */
    public static boolean isPublicHoliday(LocalDate date) {
        if (FIXED_SOLAR_HOLIDAYS.contains(MonthDay.from(date))) {
            return true;
        }
        return VARIABLE_HOLIDAYS.getOrDefault(date.getYear(), Set.of()).contains(date);
    }

    /** 근로자의 날인가 (사업장 규모 무관 유급휴일) */
    public static boolean isLaborDay(LocalDate date) {
        return date.getMonthValue() == 5 && date.getDayOfMonth() == 1;
    }

    /**
     * 해당 사업장에 유급휴일로 적용되는 날인가.
     * - 5인 이상: 관공서 공휴일 + 근로자의 날
     * - 5인 미만: 근로자의 날만 (공휴일 유급 적용 의무 없음)
     */
    public static boolean isPaidHoliday(LocalDate date, boolean fiveOrMoreEmployees) {
        if (isLaborDay(date)) {
            return true;
        }
        return fiveOrMoreEmployees && isPublicHoliday(date);
    }

    /** 변동 공휴일 데이터가 준비된 연도인가 (미준비 연도는 고정 공휴일만 인식) */
    public static boolean hasVariableDataFor(int year) {
        return VARIABLE_HOLIDAYS.containsKey(year);
    }
}
