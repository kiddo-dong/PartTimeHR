package com.example.PartTimeHR.workrecord.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkRecordTest {

    private WorkRecord inProgressRecord() {
        return WorkRecord.builder()
                .workDate(LocalDate.of(2026, 7, 13))
                .clockInTime(LocalDateTime.of(2026, 7, 13, 9, 0))
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .status(WorkStatus.IN_PROGRESS)
                .totalBreakMinutes(0)
                .totalWorkedMinutes(0)
                .netWorkedMinutes(0)
                .build();
    }

    @Test
    void 휴게를_여러_번_시작하고_종료할_수_있다() {
        WorkRecord record = inProgressRecord();

        record.startBreak();
        assertThat(record.getStatus()).isEqualTo(WorkStatus.ON_BREAK);

        record.endBreak();
        assertThat(record.getStatus()).isEqualTo(WorkStatus.IN_PROGRESS);

        // 두 번째 휴게도 가능해야 한다
        assertThat(record.canStartBreak()).isTrue();
        record.startBreak();
        assertThat(record.getStatus()).isEqualTo(WorkStatus.ON_BREAK);
    }

    @Test
    void 휴게_중에는_휴게_시작과_퇴근이_불가능하다() {
        WorkRecord record = inProgressRecord();
        record.startBreak();

        assertThatThrownBy(record::startBreak)
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> record.clockOut(LocalDateTime.of(2026, 7, 13, 18, 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 퇴근하면_상태가_완료되고_집계가_확정된다() {
        WorkRecord record = WorkRecord.builder()
                .workDate(LocalDate.of(2026, 7, 13))
                .clockInTime(LocalDateTime.of(2026, 7, 13, 9, 0))
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .status(WorkStatus.IN_PROGRESS)
                .totalBreakMinutes(30) // 휴게 30분 누적 상태
                .totalWorkedMinutes(0)
                .netWorkedMinutes(0)
                .build();

        record.clockOut(LocalDateTime.of(2026, 7, 13, 17, 0)); // 8시간 근무

        assertThat(record.getStatus()).isEqualTo(WorkStatus.COMPLETED);
        assertThat(record.getTotalWorkedMinutes()).isEqualTo(480);
        assertThat(record.getBreakMinutes()).isEqualTo(30);
        assertThat(record.getNetWorkedMinutes()).isEqualTo(450);
    }

    @Test
    void 수동_입력된_휴게_쌍으로_누적_휴게를_설정할_수_있다() {
        WorkRecord record = WorkRecord.builder()
                .workDate(LocalDate.of(2026, 7, 13))
                .clockInTime(LocalDateTime.of(2026, 7, 13, 9, 0))
                .breakStartTime(LocalDateTime.of(2026, 7, 13, 12, 0))
                .breakEndTime(LocalDateTime.of(2026, 7, 13, 13, 0))
                .clockOutTime(LocalDateTime.of(2026, 7, 13, 18, 0))
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .status(WorkStatus.COMPLETED)
                .totalBreakMinutes(0)
                .totalWorkedMinutes(0)
                .netWorkedMinutes(0)
                .build();

        record.applyBreakFromTimes();
        record.recalculateMinutes();

        assertThat(record.getBreakMinutes()).isEqualTo(60);
        assertThat(record.getTotalWorkedMinutes()).isEqualTo(540);
        assertThat(record.getNetWorkedMinutes()).isEqualTo(480);
    }

    @Test
    void 퇴근이_출근보다_빠르면_검증에_실패한다() {
        WorkRecord record = WorkRecord.builder()
                .workDate(LocalDate.of(2026, 7, 13))
                .clockInTime(LocalDateTime.of(2026, 7, 13, 18, 0))
                .clockOutTime(LocalDateTime.of(2026, 7, 13, 9, 0)) // 출근보다 빠름
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .status(WorkStatus.IN_PROGRESS)
                .totalBreakMinutes(0)
                .totalWorkedMinutes(0)
                .netWorkedMinutes(0)
                .build();

        assertThatThrownBy(record::validateTimes)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("퇴근 시간");
    }

    @Test
    void 시간으로부터_상태를_재도출한다() {
        WorkRecord record = WorkRecord.builder()
                .workDate(LocalDate.of(2026, 7, 13))
                .clockInTime(LocalDateTime.of(2026, 7, 13, 9, 0))
                .clockOutTime(LocalDateTime.of(2026, 7, 13, 18, 0))
                .appliedHourlyWage(10320)
                .appliedJobTitle("알바생")
                .status(WorkStatus.IN_PROGRESS) // 퇴근 시간과 불일치한 상태
                .totalBreakMinutes(0)
                .totalWorkedMinutes(0)
                .netWorkedMinutes(0)
                .build();

        record.refreshStatus();

        assertThat(record.getStatus()).isEqualTo(WorkStatus.COMPLETED);
    }

    @Test
    void 퇴근_전에는_총_근무_시간이_확정되지_않는다() {
        WorkRecord record = inProgressRecord();

        assertThat(record.getTotalWorkMinutes()).isNull();
        assertThat(record.getActualWorkMinutes()).isNull();
        assertThat(record.isActive()).isTrue();
    }
}
