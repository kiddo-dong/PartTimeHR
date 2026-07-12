package com.example.PartTimeHR.workrecord.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkRecordTest {

    private static final LocalDate DATE = LocalDate.of(2026, 7, 13);

    private LocalDateTime at(int hour, int minute) {
        return DATE.atTime(hour, minute);
    }

    private WorkRecord inProgressRecord() {
        return WorkRecord.builder()
                .workDate(DATE)
                .clockInTime(at(9, 0))
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .build();
    }

    @Test
    void 상태는_시각에서_유도된다() {
        WorkRecord record = inProgressRecord();
        assertThat(record.getStatus()).isEqualTo(WorkStatus.IN_PROGRESS);
        assertThat(record.isActive()).isTrue();

        record.startBreak(at(12, 0));
        assertThat(record.getStatus()).isEqualTo(WorkStatus.ON_BREAK);

        record.endBreak(at(12, 30));
        assertThat(record.getStatus()).isEqualTo(WorkStatus.IN_PROGRESS);

        record.clockOut(at(18, 0));
        assertThat(record.getStatus()).isEqualTo(WorkStatus.COMPLETED);
        assertThat(record.isActive()).isFalse();
    }

    @Test
    void 휴게를_여러_번_해도_각각_이력이_남고_누적된다() {
        WorkRecord record = inProgressRecord();

        record.startBreak(at(12, 0));
        record.endBreak(at(12, 30));   // 30분

        record.startBreak(at(15, 0));
        record.endBreak(at(15, 20));   // 20분

        record.clockOut(at(18, 0));

        assertThat(record.getBreaks()).hasSize(2);
        assertThat(record.getBreakMinutes()).isEqualTo(50);
        assertThat(record.getTotalWorkMinutes()).isEqualTo(540);   // 9:00 ~ 18:00
        assertThat(record.getActualWorkMinutes()).isEqualTo(490);
    }

    @Test
    void 휴게_중에는_휴게_시작과_퇴근이_불가능하다() {
        WorkRecord record = inProgressRecord();
        record.startBreak(at(12, 0));

        assertThatThrownBy(() -> record.startBreak(at(12, 10)))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> record.clockOut(at(18, 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 수동_입력된_휴게_쌍으로_교체할_수_있다() {
        WorkRecord record = WorkRecord.builder()
                .workDate(DATE)
                .clockInTime(at(9, 0))
                .clockOutTime(at(18, 0))
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .build();

        record.replaceBreaks(at(12, 0), at(13, 0));
        record.validateTimes();

        assertThat(record.getBreakMinutes()).isEqualTo(60);
        assertThat(record.getActualWorkMinutes()).isEqualTo(480);
        assertThat(record.getBreakStartTime()).isEqualTo(at(12, 0));
        assertThat(record.getBreakEndTime()).isEqualTo(at(13, 0));
    }

    @Test
    void 퇴근이_출근보다_빠르면_검증에_실패한다() {
        WorkRecord record = WorkRecord.builder()
                .workDate(DATE)
                .clockInTime(at(18, 0))
                .clockOutTime(at(9, 0)) // 출근보다 빠름
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .build();

        assertThatThrownBy(record::validateTimes)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("퇴근 시간");
    }

    @Test
    void 근무_구간_밖의_휴게는_검증에_실패한다() {
        WorkRecord record = WorkRecord.builder()
                .workDate(DATE)
                .clockInTime(at(9, 0))
                .clockOutTime(at(18, 0))
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .build();

        record.replaceBreaks(at(8, 0), at(8, 30)); // 출근 전 휴게

        assertThatThrownBy(record::validateTimes)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("휴게");
    }

    @Test
    void 미퇴근_근무는_자동_마감된다() {
        WorkRecord record = WorkRecord.builder()
                .workDate(DATE)
                .clockInTime(at(18, 0))
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .build();

        record.autoClose();

        assertThat(record.getStatus()).isEqualTo(WorkStatus.COMPLETED);
        assertThat(record.getClockOutTime()).isEqualTo(at(23, 59));
        assertThat(record.getTotalWorkMinutes()).isEqualTo(359); // 18:00 ~ 23:59
        assertThat(record.getMemo()).contains("미퇴근 자동 마감");
    }

    @Test
    void 휴게_중에_방치된_근무는_휴게를_마감_시각까지로_보고_자동_마감된다() {
        WorkRecord record = WorkRecord.builder()
                .workDate(DATE)
                .clockInTime(at(18, 0))
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .build();
        record.startBreak(at(20, 0));

        record.autoClose();

        assertThat(record.getStatus()).isEqualTo(WorkStatus.COMPLETED);
        assertThat(record.getBreakMinutes()).isEqualTo(239);       // 20:00 ~ 23:59
        assertThat(record.getActualWorkMinutes()).isEqualTo(120);  // 18:00 ~ 20:00
    }

    @Test
    void 퇴근_전에는_총_근무_시간이_확정되지_않는다() {
        WorkRecord record = inProgressRecord();

        assertThat(record.getTotalWorkMinutes()).isNull();
        assertThat(record.getActualWorkMinutes()).isNull();
    }

    @Test
    void 부분_수정은_null_필드를_기존_값으로_유지한다() {
        WorkRecord record = WorkRecord.builder()
                .workDate(DATE)
                .clockInTime(at(9, 0))
                .clockOutTime(at(18, 0))
                .memo("원래 메모")
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .build();

        record.updateManually(null, at(19, 0), null);

        assertThat(record.getClockInTime()).isEqualTo(at(9, 0));   // 유지
        assertThat(record.getClockOutTime()).isEqualTo(at(19, 0)); // 변경
        assertThat(record.getMemo()).isEqualTo("원래 메모");        // 유지
    }
}
