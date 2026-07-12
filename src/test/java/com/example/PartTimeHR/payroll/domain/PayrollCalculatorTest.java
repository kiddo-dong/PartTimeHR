package com.example.PartTimeHR.payroll.domain;

import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PayrollCalculatorTest {

    private static final int MONDAY = 1;

    // 2026-07-13(월)부터 시작하는 한 주
    private static final LocalDate MON = LocalDate.of(2026, 7, 13);

    private WorkRecord record(LocalDate workDate, int netMinutes, int wage) {
        return WorkRecord.builder()
                .workDate(workDate)
                .clockInTime(workDate.atTime(9, 0))
                .clockOutTime(workDate.atTime(9, 0).plusMinutes(netMinutes))
                .appliedHourlyWage(wage)
                .appliedJobTitle("알바생")
                .build();
    }

    @Test
    void 기본급은_실근무_분에_시급_스냅샷을_곱해_계산한다() {
        // 8시간 × 10,000원 = 80,000원
        WorkRecord record = record(MON, 480, 10000);

        assertThat(PayrollCalculator.recordPay(record)).isEqualTo(80000);
    }

    @Test
    void 주_15시간_이상이면_주휴수당이_붙는다() {
        // 한 주에 10시간씩 2일 = 20시간, 시급 10,000원
        List<WorkRecord> records = List.of(
                record(MON, 600, 10000),
                record(MON.plusDays(2), 600, 10000)
        );

        PayrollCalculator.Result result =
                PayrollCalculator.calculate(records, MONDAY, true);

        assertThat(result.basePay()).isEqualTo(200000);
        // 20 / 40 × 8시간 × 10,000원 = 40,000원
        assertThat(result.weeklyAllowance()).isEqualTo(40000);
        assertThat(result.totalPay()).isEqualTo(240000);
    }

    @Test
    void 주_15시간_미만이면_주휴수당이_없다() {
        // 한 주 14시간
        List<WorkRecord> records = List.of(
                record(MON, 480, 10000),
                record(MON.plusDays(1), 360, 10000)
        );

        PayrollCalculator.Result result =
                PayrollCalculator.calculate(records, MONDAY, true);

        assertThat(result.weeklyAllowance()).isEqualTo(0);
        assertThat(result.totalPay()).isEqualTo(result.basePay());
    }

    @Test
    void 매장이_주휴_미적용이면_시간과_무관하게_주휴수당이_없다() {
        List<WorkRecord> records = List.of(
                record(MON, 600, 10000),
                record(MON.plusDays(2), 600, 10000)
        );

        PayrollCalculator.Result result =
                PayrollCalculator.calculate(records, MONDAY, false);

        assertThat(result.weeklyAllowance()).isEqualTo(0);
    }

    @Test
    void 주가_다르면_주휴수당은_주별로_따로_판정한다() {
        // 1주차 20시간(주휴 O), 2주차 10시간(주휴 X)
        List<WorkRecord> records = List.of(
                record(MON, 600, 10000),
                record(MON.plusDays(1), 600, 10000),
                record(MON.plusDays(7), 600, 10000)
        );

        PayrollCalculator.Result result =
                PayrollCalculator.calculate(records, MONDAY, true);

        assertThat(result.weeklyAllowance()).isEqualTo(40000); // 1주차만
    }
}
