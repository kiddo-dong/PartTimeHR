package com.example.PartTimeHR.attendance.application;

import com.example.PartTimeHR.attendance.presentation.dto.AttendanceDailyResponse;
import com.example.PartTimeHR.attendance.presentation.dto.AttendanceMatchStatus;
import com.example.PartTimeHR.attendance.presentation.dto.AttendanceSummaryResponse;
import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.domain.ScheduleRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.application.StoreAccessService;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkStatus;
import com.example.PartTimeHR.workrecord.domain.WorkRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private StoreAccessService storeAccessService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private WorkRecordRepository workRecordRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void getDailyAttendance_returnsAbsent_whenScheduledButNoRecord() {
        Long employerId = 1L;
        Long storeId = 10L;
        LocalDate date = LocalDate.of(2026, 1, 10);

        Store store = store(storeId);
        Employee employee = employee(100L, "Alice", store);

        Schedule schedule = Schedule.builder()
                .id(11L)
                .store(store)
                .employee(employee)
                .workDate(date)
                .startTime(LocalDateTime.of(2026, 1, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 1, 10, 18, 0))
                .confirmed(true)
                .build();

        when(storeAccessService.getMyStore(storeId, employerId)).thenReturn(store);
        when(scheduleRepository.findByStoreAndWorkDate(store, date)).thenReturn(List.of(schedule));
        when(workRecordRepository.findAllByStoreAndWorkDate(storeId, date)).thenReturn(List.of());

        AttendanceDailyResponse result = attendanceService.getDailyAttendance(employerId, storeId, date);

        assertThat(result.getAbsentCount()).isEqualTo(1);
        assertThat(result.getScheduledCount()).isEqualTo(1);
        assertThat(result.getWorkedCount()).isEqualTo(0);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getStatus()).isEqualTo(AttendanceMatchStatus.ABSENT);
    }

    @Test
    void getDailyAttendance_returnsScheduled_whenScheduleNotEndedYet() {
        Long employerId = 1L;
        Long storeId = 10L;
        LocalDate date = LocalDate.now().plusDays(1); // 미래 날짜 - 아직 결근 아님

        Store store = store(storeId);
        Employee employee = employee(100L, "Dana", store);

        Schedule schedule = Schedule.builder()
                .id(12L)
                .store(store)
                .employee(employee)
                .workDate(date)
                .startTime(date.atTime(9, 0))
                .endTime(date.atTime(18, 0))
                .confirmed(true)
                .build();

        when(storeAccessService.getMyStore(storeId, employerId)).thenReturn(store);
        when(scheduleRepository.findByStoreAndWorkDate(store, date)).thenReturn(List.of(schedule));
        when(workRecordRepository.findAllByStoreAndWorkDate(storeId, date)).thenReturn(List.of());

        AttendanceDailyResponse result = attendanceService.getDailyAttendance(employerId, storeId, date);

        assertThat(result.getScheduledCount()).isEqualTo(1);
        assertThat(result.getAbsentCount()).isEqualTo(0);
        assertThat(result.getItems().get(0).getStatus()).isEqualTo(AttendanceMatchStatus.SCHEDULED);
    }

    @Test
    void getDailyAttendance_returnsLate_whenClockInAfterScheduleStart() {
        Long employerId = 1L;
        Long storeId = 10L;
        LocalDate date = LocalDate.of(2026, 1, 10);

        Store store = store(storeId);
        Employee employee = employee(100L, "Bob", store);

        Schedule schedule = Schedule.builder()
                .id(21L)
                .store(store)
                .employee(employee)
                .workDate(date)
                .startTime(LocalDateTime.of(2026, 1, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 1, 10, 18, 0))
                .confirmed(true)
                .build();

        WorkRecord record = WorkRecord.builder()
                .id(31L)
                .employee(employee)
                .workDate(date)
                .clockInTime(LocalDateTime.of(2026, 1, 10, 9, 15))
                .clockOutTime(LocalDateTime.of(2026, 1, 10, 18, 0))
                .appliedHourlyWage(11000)
                .appliedJobTitle("Crew")
                .status(WorkStatus.COMPLETED)
                .totalBreakMinutes(0)
                .totalWorkedMinutes(525)
                .netWorkedMinutes(525)
                .build();

        when(storeAccessService.getMyStore(storeId, employerId)).thenReturn(store);
        when(scheduleRepository.findByStoreAndWorkDate(store, date)).thenReturn(List.of(schedule));
        when(workRecordRepository.findAllByStoreAndWorkDate(storeId, date)).thenReturn(List.of(record));

        AttendanceDailyResponse result = attendanceService.getDailyAttendance(employerId, storeId, date);

        assertThat(result.getLateCount()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getStatus()).isEqualTo(AttendanceMatchStatus.LATE);
        assertThat(result.getItems().get(0).getLateMinutes()).isEqualTo(15);
    }

    @Test
    void getSummary_aggregatesInSinglePassRange() {
        Long employerId = 1L;
        Long storeId = 10L;
        LocalDate from = LocalDate.of(2026, 1, 10);
        LocalDate to = LocalDate.of(2026, 1, 11);

        Store store = store(storeId);
        Employee employee = employee(100L, "Chris", store);

        Schedule d1Schedule = Schedule.builder()
                .id(41L)
                .store(store)
                .employee(employee)
                .workDate(from)
                .startTime(LocalDateTime.of(2026, 1, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 1, 10, 18, 0))
                .confirmed(true)
                .build();

        Schedule d2Schedule = Schedule.builder()
                .id(42L)
                .store(store)
                .employee(employee)
                .workDate(to)
                .startTime(LocalDateTime.of(2026, 1, 11, 9, 0))
                .endTime(LocalDateTime.of(2026, 1, 11, 18, 0))
                .confirmed(true)
                .build();

        WorkRecord d2Record = WorkRecord.builder()
                .id(51L)
                .employee(employee)
                .workDate(to)
                .clockInTime(LocalDateTime.of(2026, 1, 11, 9, 0))
                .clockOutTime(LocalDateTime.of(2026, 1, 11, 18, 0))
                .appliedHourlyWage(11000)
                .appliedJobTitle("Crew")
                .status(WorkStatus.COMPLETED)
                .totalBreakMinutes(0)
                .totalWorkedMinutes(540)
                .netWorkedMinutes(540)
                .build();

        when(storeAccessService.getMyStore(storeId, employerId)).thenReturn(store);
        when(scheduleRepository.findByStoreAndWorkDateBetween(store, from, to)).thenReturn(List.of(d1Schedule, d2Schedule));
        when(workRecordRepository.findAllByStoreAndWorkDateBetween(storeId, from, to)).thenReturn(List.of(d2Record));

        AttendanceSummaryResponse result = attendanceService.getSummary(employerId, storeId, from, to);

        assertThat(result.getScheduledCount()).isEqualTo(2);
        assertThat(result.getAbsentCount()).isEqualTo(1);
        assertThat(result.getWorkedCount()).isEqualTo(1);
        assertThat(result.getAttendanceRate()).isEqualTo(50.0);
    }

    @Test
    void getSummary_throws_whenDateRangeIsInvalid() {
        assertThatThrownBy(() -> attendanceService.getSummary(1L, 1L,
                LocalDate.of(2026, 1, 11),
                LocalDate.of(2026, 1, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종료일");
    }

    private Store store(Long storeId) {
        return Store.builder()
                .id(storeId)
                .name("Store")
                .weekStartDay(1)
                .weeklyPayApplicable(false)
                .build();
    }

    private Employee employee(Long id, String name, Store store) {
        return Employee.builder()
                .id(id)
                .name(name)
                .email(name.toLowerCase() + "@test.com")
                .password("encoded")
                .phone("01012341234")
                .store(store)
                .role(Role.ROLE_EMPLOYEE)
                .build();
    }
}
