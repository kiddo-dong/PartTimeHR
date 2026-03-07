package com.example.PartTimeHR.attendance.service;

import com.example.PartTimeHR.attendance.dto.AttendanceDailyResponse;
import com.example.PartTimeHR.attendance.dto.AttendanceMatchStatus;
import com.example.PartTimeHR.attendance.dto.AttendanceSummaryResponse;
import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.repository.ScheduleRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.service.StoreAccessService;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkStatus;
import com.example.PartTimeHR.workrecord.repository.WorkRecordRepository;
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

        Schedule schedule = schedule(employee, date,
                LocalDateTime.of(2026, 1, 10, 9, 0),
                LocalDateTime.of(2026, 1, 10, 18, 0));

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
    void getDailyAttendance_returnsLate_whenClockInAfterMergedScheduleStart() {
        Long employerId = 1L;
        Long storeId = 10L;
        LocalDate date = LocalDate.of(2026, 1, 10);

        Store store = store(storeId);
        Employee employee = employee(100L, "Bob", store);

        Schedule s1 = schedule(employee, date,
                LocalDateTime.of(2026, 1, 10, 9, 0),
                LocalDateTime.of(2026, 1, 10, 12, 0));
        Schedule s2 = schedule(employee, date,
                LocalDateTime.of(2026, 1, 10, 13, 0),
                LocalDateTime.of(2026, 1, 10, 18, 0));

        WorkRecord record = completedRecord(employee, date,
                LocalDateTime.of(2026, 1, 10, 9, 15),
                LocalDateTime.of(2026, 1, 10, 18, 0),
                525);

        when(storeAccessService.getMyStore(storeId, employerId)).thenReturn(store);
        when(scheduleRepository.findByStoreAndWorkDate(store, date)).thenReturn(List.of(s1, s2));
        when(workRecordRepository.findAllByStoreAndWorkDate(storeId, date)).thenReturn(List.of(record));

        AttendanceDailyResponse result = attendanceService.getDailyAttendance(employerId, storeId, date);

        assertThat(result.getLateCount()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getStatus()).isEqualTo(AttendanceMatchStatus.LATE);
        assertThat(result.getItems().get(0).getLateMinutes()).isEqualTo(15);
    }

    @Test
    void getDailyAttendance_returnsEarlyLeave_whenClockOutBeforeMergedScheduleEnd() {
        Long employerId = 1L;
        Long storeId = 10L;
        LocalDate date = LocalDate.of(2026, 1, 10);

        Store store = store(storeId);
        Employee employee = employee(100L, "Carol", store);

        Schedule s1 = schedule(employee, date,
                LocalDateTime.of(2026, 1, 10, 9, 0),
                LocalDateTime.of(2026, 1, 10, 12, 0));
        Schedule s2 = schedule(employee, date,
                LocalDateTime.of(2026, 1, 10, 13, 0),
                LocalDateTime.of(2026, 1, 10, 18, 0));

        WorkRecord record = completedRecord(employee, date,
                LocalDateTime.of(2026, 1, 10, 9, 0),
                LocalDateTime.of(2026, 1, 10, 17, 30),
                510);

        when(storeAccessService.getMyStore(storeId, employerId)).thenReturn(store);
        when(scheduleRepository.findByStoreAndWorkDate(store, date)).thenReturn(List.of(s1, s2));
        when(workRecordRepository.findAllByStoreAndWorkDate(storeId, date)).thenReturn(List.of(record));

        AttendanceDailyResponse result = attendanceService.getDailyAttendance(employerId, storeId, date);

        assertThat(result.getItems().get(0).getStatus()).isEqualTo(AttendanceMatchStatus.EARLY_LEAVE);
        assertThat(result.getItems().get(0).getEarlyLeaveMinutes()).isEqualTo(30);
    }

    @Test
    void getSummary_aggregatesRangeCounts() {
        Long employerId = 1L;
        Long storeId = 10L;
        LocalDate from = LocalDate.of(2026, 1, 10);
        LocalDate to = LocalDate.of(2026, 1, 11);

        Store store = store(storeId);
        Employee employee = employee(100L, "Chris", store);

        Schedule d1Schedule = schedule(employee, from,
                LocalDateTime.of(2026, 1, 10, 9, 0),
                LocalDateTime.of(2026, 1, 10, 18, 0));
        Schedule d2Schedule = schedule(employee, to,
                LocalDateTime.of(2026, 1, 11, 9, 0),
                LocalDateTime.of(2026, 1, 11, 18, 0));

        WorkRecord d2Record = completedRecord(employee, to,
                LocalDateTime.of(2026, 1, 11, 9, 0),
                LocalDateTime.of(2026, 1, 11, 18, 0),
                540);

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

    @Test
    void getDailyAttendance_throws_whenDateIsNull() {
        assertThatThrownBy(() -> attendanceService.getDailyAttendance(1L, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("날짜");
    }

    private Schedule schedule(Employee employee, LocalDate workDate, LocalDateTime start, LocalDateTime end) {
        return Schedule.builder()
                .store(employee.getStore())
                .employee(employee)
                .workDate(workDate)
                .startTime(start)
                .endTime(end)
                .confirmed(true)
                .build();
    }

    private WorkRecord completedRecord(
            Employee employee,
            LocalDate workDate,
            LocalDateTime clockIn,
            LocalDateTime clockOut,
            int netMinutes
    ) {
        return WorkRecord.builder()
                .employee(employee)
                .workDate(workDate)
                .clockInTime(clockIn)
                .clockOutTime(clockOut)
                .appliedHourlyWage(11000)
                .appliedJobTitle("Crew")
                .status(WorkStatus.COMPLETED)
                .totalBreakMinutes(0)
                .totalWorkedMinutes(netMinutes)
                .netWorkedMinutes(netMinutes)
                .build();
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
