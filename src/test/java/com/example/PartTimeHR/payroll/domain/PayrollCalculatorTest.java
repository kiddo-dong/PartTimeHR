package com.example.PartTimeHR.payroll.domain;

import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PayrollCalculatorTest {

    private static final int MONDAY = 1;

    // 2026-07-13(월)부터 시작하는 한 주
    private static final LocalDate MON = LocalDate.of(2026, 7, 13);

    private WorkRecord record(LocalDate workDate, int netMinutes, int wage) {
        return record(workDate.atTime(9, 0), netMinutes, wage);
    }

    private WorkRecord record(LocalDateTime clockIn, int netMinutes, int wage) {
        return WorkRecord.builder()
                .workDate(clockIn.toLocalDate())
                .clockInTime(clockIn)
                .clockOutTime(clockIn.plusMinutes(netMinutes))
                .appliedHourlyWage(wage)
                .appliedJobTitle("알바생")
                .build();
    }

    private Schedule schedule(LocalDate workDate) {
        return Schedule.builder()
                .workDate(workDate)
                .startTime(workDate.atTime(9, 0))
                .endTime(workDate.atTime(18, 0))
                .build();
    }

    // calculateWeekly=true → 주휴 별도 계산 (시급 포함 계약이 아님), 현재 시급 10,000원 가정
    private PayrollCalculator.Result calc(List<WorkRecord> records, List<Schedule> schedules,
                                          boolean calculateWeekly, boolean fiveOrMore) {
        return PayrollCalculator.calculate(records, schedules, List.of(),
                new PayrollCalculator.Params(MONDAY, !calculateWeekly, fiveOrMore, 10000, null));
    }

    @Test
    void 기본급은_실근무_분에_시급_스냅샷을_곱해_계산한다() {
        // 8시간 × 10,000원 = 80,000원
        WorkRecord record = record(MON, 480, 10000);

        assertThat(PayrollCalculator.recordPay(record)).isEqualTo(80000);
    }

    @Test
    void 주_15시간_이상이고_개근이면_주휴수당이_붙는다() {
        // 한 주에 10시간씩 2일 = 20시간, 시급 10,000원 (스케줄 2일 모두 근무 → 개근)
        List<WorkRecord> records = List.of(
                record(MON, 600, 10000),
                record(MON.plusDays(2), 600, 10000)
        );
        List<Schedule> schedules = List.of(schedule(MON), schedule(MON.plusDays(2)));

        PayrollCalculator.Result result = calc(records, schedules, true, false);

        assertThat(result.basePay()).isEqualTo(200000);
        // 20 / 40 × 8시간 × 10,000원 = 40,000원
        assertThat(result.weeklyAllowance()).isEqualTo(40000);
        assertThat(result.totalPay()).isEqualTo(240000);
    }

    @Test
    void 결근이_있으면_시간이_충분해도_주휴수당이_없다() {
        // 스케줄 3일 중 2일만 근무 (결근 1일) - 20시간이지만 개근 실패
        List<WorkRecord> records = List.of(
                record(MON, 600, 10000),
                record(MON.plusDays(2), 600, 10000)
        );
        List<Schedule> schedules = List.of(
                schedule(MON), schedule(MON.plusDays(2)), schedule(MON.plusDays(4)) // 금요일 결근
        );

        PayrollCalculator.Result result = calc(records, schedules, true, false);

        assertThat(result.weeklyAllowance()).isEqualTo(0);
    }

    @Test
    void 주_15시간_미만이면_주휴수당이_없다() {
        List<WorkRecord> records = List.of(
                record(MON, 480, 10000),
                record(MON.plusDays(1), 360, 10000)
        );

        PayrollCalculator.Result result = calc(records, List.of(), true, false);

        assertThat(result.weeklyAllowance()).isEqualTo(0);
    }

    @Test
    void 일_8시간_초과분은_50퍼센트_가산된다_5인_이상() {
        // 하루 10시간 근무 → 연장 2시간, 시급 10,000원
        List<WorkRecord> records = List.of(record(MON, 600, 10000));

        PayrollCalculator.Result result = calc(records, List.of(), false, true);

        // 2시간 × 10,000 × 0.5 = 10,000원
        assertThat(result.overtimeAllowance()).isEqualTo(10000);
    }

    @Test
    void 상시_5인_미만이면_연장_야간_가산이_없다() {
        // 하루 10시간 + 야간대 근무여도 가산 없음
        List<WorkRecord> records = List.of(
                record(MON.atTime(20, 0), 600, 10000) // 20:00~06:00
        );

        PayrollCalculator.Result result = calc(records, List.of(), false, false);

        assertThat(result.overtimeAllowance()).isEqualTo(0);
        assertThat(result.nightAllowance()).isEqualTo(0);
    }

    @Test
    void 야간_근로분은_50퍼센트_가산된다_5인_이상() {
        // 18:00~24:00 근무 (6시간) → 야간(22~24시) 2시간, 시급 10,000원
        List<WorkRecord> records = List.of(
                record(MON.atTime(18, 0), 360, 10000)
        );

        PayrollCalculator.Result result = calc(records, List.of(), false, true);

        // 야간 2시간 × 10,000 × 0.5 = 10,000원
        assertThat(result.nightAllowance()).isEqualTo(10000);
        assertThat(result.overtimeAllowance()).isEqualTo(0); // 6시간이라 연장 없음
    }

    @Test
    void 자정을_넘는_근무의_야간분도_계산된다() {
        // 22:00~다음날 02:00 (4시간 전부 야간)
        List<WorkRecord> records = List.of(
                record(MON.atTime(22, 0), 240, 10000)
        );

        PayrollCalculator.Result result = calc(records, List.of(), false, true);

        assertThat(result.nightAllowance()).isEqualTo(20000); // 4h × 10,000 × 0.5
    }

    @Test
    void 야간_계산에서_휴게_구간은_제외된다() {
        // 21:00~다음날 01:00, 휴게 23:00~24:00 → 야간 실근로 = 22~23시 + 00~01시 = 2시간
        WorkRecord withBreak = WorkRecord.builder()
                .workDate(MON)
                .clockInTime(MON.atTime(21, 0))
                .clockOutTime(MON.plusDays(1).atTime(1, 0))
                .appliedHourlyWage(10000)
                .appliedJobTitle("알바생")
                .build();
        withBreak.replaceBreaks(MON.atTime(23, 0), MON.plusDays(1).atStartOfDay());

        assertThat(PayrollCalculator.nightMinutes(withBreak)).isEqualTo(120);
    }

    @Test
    void 공휴일_근로는_50퍼센트_가산된다_5인_이상() {
        // 2026-10-09 한글날(금) 8시간 근무, 시급 10,000원
        LocalDate hangulDay = LocalDate.of(2026, 10, 9);
        List<WorkRecord> records = List.of(record(hangulDay, 480, 10000));

        PayrollCalculator.Result result = calc(records, List.of(), false, true);

        // 8시간 × 10,000 × 0.5 = 40,000원
        assertThat(result.holidayAllowance()).isEqualTo(40000);
        assertThat(result.overtimeAllowance()).isEqualTo(0); // 연장과 중복 가산 없음
    }

    @Test
    void 공휴일_8시간_초과분은_100퍼센트_가산된다() {
        // 한글날 10시간 근무: 8h × 0.5 + 2h × 1.0
        LocalDate hangulDay = LocalDate.of(2026, 10, 9);
        List<WorkRecord> records = List.of(record(hangulDay, 600, 10000));

        PayrollCalculator.Result result = calc(records, List.of(), false, true);

        assertThat(result.holidayAllowance()).isEqualTo(40000 + 20000);
        assertThat(result.overtimeAllowance()).isEqualTo(0); // 초과분은 휴일 100%로만
    }

    @Test
    void 상시_5인_미만은_공휴일_가산이_없다() {
        LocalDate hangulDay = LocalDate.of(2026, 10, 9);
        List<WorkRecord> records = List.of(record(hangulDay, 480, 10000));

        PayrollCalculator.Result result = calc(records, List.of(), false, false);

        assertThat(result.holidayAllowance()).isEqualTo(0);
    }

    @Test
    void 대체공휴일과_설연휴도_휴일로_인식된다() {
        // 2026-03-02 삼일절 대체공휴일, 2026-02-17 설날
        assertThat(KoreanHolidayCalendar.isPublicHoliday(LocalDate.of(2026, 3, 2))).isTrue();
        assertThat(KoreanHolidayCalendar.isPublicHoliday(LocalDate.of(2026, 2, 17))).isTrue();
        assertThat(KoreanHolidayCalendar.isPublicHoliday(LocalDate.of(2026, 7, 13))).isFalse();

        // 근로자의 날은 규모 무관 유급휴일
        assertThat(KoreanHolidayCalendar.isPaidHoliday(LocalDate.of(2026, 5, 1), false)).isTrue();
        // 5인 미만은 관공서 공휴일 미적용
        assertThat(KoreanHolidayCalendar.isPaidHoliday(LocalDate.of(2026, 10, 9), false)).isFalse();
    }

    @Test
    void 공휴일에_스케줄이_있었지만_쉰_것은_개근을_깨지_않는다() {
        // 화·수(평일) + 금(한글날 2026-10-09) 스케줄 - 한글날은 쉬었지만 개근 유지
        LocalDate tue = LocalDate.of(2026, 10, 6);
        LocalDate wed = LocalDate.of(2026, 10, 7);
        LocalDate fri = LocalDate.of(2026, 10, 9); // 한글날

        List<WorkRecord> records = List.of(
                record(tue, 600, 10000),
                record(wed, 600, 10000)
        );
        List<Schedule> schedules = List.of(
                schedule(tue), schedule(wed), schedule(fri) // 한글날 스케줄은 쉼
        );

        PayrollCalculator.Result result = calc(records, schedules, true, true);

        // 20시간 + 개근(한글날 제외) → 주휴수당 지급
        assertThat(result.weeklyAllowance()).isEqualTo(40000);
    }

    @Test
    void 유급휴일에_스케줄이_있고_쉬면_유급휴일수당이_지급된다() {
        // 한글날(2026-10-09) 9~18시 스케줄, 근무 기록 없음 (쉼)
        LocalDate hangulDay = LocalDate.of(2026, 10, 9);
        List<Schedule> schedules = List.of(schedule(hangulDay));

        PayrollCalculator.Result result = calc(List.of(), schedules, true, true);

        // 소정 9시간 → 8시간 한도 × 10,000원 = 80,000원 (기본급 없이 유급분만)
        assertThat(result.basePay()).isEqualTo(0);
        assertThat(result.holidayLeavePay()).isEqualTo(80000);
        assertThat(result.totalPay()).isEqualTo(80000);
    }

    @Test
    void 유급휴일에_스케줄대로_근무하면_통상_2_5배가_된다() {
        // 한글날 스케줄 + 8시간 근무: 근로임금(1.0) + 휴일가산(0.5) + 유급분(1.0)
        LocalDate hangulDay = LocalDate.of(2026, 10, 9);
        List<WorkRecord> records = List.of(record(hangulDay, 480, 10000));
        List<Schedule> schedules = List.of(schedule(hangulDay));

        PayrollCalculator.Result result = calc(records, schedules, false, true);

        assertThat(result.basePay()).isEqualTo(80000);          // 1.0
        assertThat(result.holidayAllowance()).isEqualTo(40000); // 0.5
        assertThat(result.holidayLeavePay()).isEqualTo(80000);  // 1.0
        assertThat(result.totalPay()).isEqualTo(200000);        // 2.5배
    }

    @Test
    void 상시_5인_미만은_근로자의_날만_유급휴일수당_대상이다() {
        // 근로자의 날(2026-05-01) 스케줄 + 쉼 → 유급분 지급
        List<Schedule> laborDay = List.of(schedule(LocalDate.of(2026, 5, 1)));
        PayrollCalculator.Result withLaborDay = calc(List.of(), laborDay, true, false);
        assertThat(withLaborDay.holidayLeavePay()).isEqualTo(80000);

        // 한글날 스케줄 + 쉼 → 5인 미만은 공휴일 유급 의무 없음
        List<Schedule> hangulDay = List.of(schedule(LocalDate.of(2026, 10, 9)));
        PayrollCalculator.Result withPublicHoliday = calc(List.of(), hangulDay, true, false);
        assertThat(withPublicHoliday.holidayLeavePay()).isEqualTo(0);
    }

    @Test
    void 약정_주휴일_근무는_휴일_가산이_붙는다_5인_이상() {
        // 주휴일=일요일(7), 일요일(2026-07-19) 8시간 근무
        List<WorkRecord> records = List.of(record(MON.plusDays(6), 480, 10000));

        PayrollCalculator.Result result = PayrollCalculator.calculate(
                records, List.of(), List.of(),
                new PayrollCalculator.Params(MONDAY, true, true, 10000, 7));

        // 가산 50%만 (유급분은 주휴수당의 몫이라 holidayLeavePay 없음)
        assertThat(result.holidayAllowance()).isEqualTo(40000);
        assertThat(result.holidayLeavePay()).isEqualTo(0);
    }

    @Test
    void 승인된_연차_사용일은_연차수당이_지급되고_개근을_깨지_않는다() {
        // 월·수 근무(20시간) + 화요일 연차 (스케줄 3일)
        List<WorkRecord> records = List.of(
                record(MON, 600, 10000),
                record(MON.plusDays(2), 600, 10000)
        );
        List<Schedule> schedules = List.of(
                schedule(MON), schedule(MON.plusDays(1)), schedule(MON.plusDays(2))
        );

        PayrollCalculator.Result result = PayrollCalculator.calculate(
                records, schedules, List.of(MON.plusDays(1)),
                new PayrollCalculator.Params(MONDAY, false, false, 10000, null));

        // 연차수당: 화요일 스케줄 9시간 → 8시간 한도 × 10,000 = 80,000
        assertThat(result.annualLeavePay()).isEqualTo(80000);
        // 연차 사용은 결근이 아님 → 개근 유지 → 주휴수당 지급
        assertThat(result.weeklyAllowance()).isEqualTo(40000);
    }

    @Test
    void 주가_다르면_주휴수당은_주별로_따로_판정한다() {
        // 1주차 20시간(주휴 O), 2주차 10시간(주휴 X)
        List<WorkRecord> records = List.of(
                record(MON, 600, 10000),
                record(MON.plusDays(1), 600, 10000),
                record(MON.plusDays(7), 600, 10000)
        );

        PayrollCalculator.Result result = calc(records, List.of(), true, false);

        assertThat(result.weeklyAllowance()).isEqualTo(40000); // 1주차만
    }
}
