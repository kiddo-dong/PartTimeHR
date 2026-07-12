package com.example.PartTimeHR.attendance.application;

import com.example.PartTimeHR.attendance.presentation.dto.AttendanceDailyEmployeeResponse;
import com.example.PartTimeHR.attendance.presentation.dto.AttendanceDailyResponse;
import com.example.PartTimeHR.attendance.presentation.dto.AttendanceMatchStatus;
import com.example.PartTimeHR.attendance.presentation.dto.AttendanceSummaryResponse;
import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.domain.ScheduleRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.application.StoreAccessService;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final StoreAccessService storeAccessService;
    private final ScheduleRepository scheduleRepository;
    private final WorkRecordRepository workRecordRepository;

    public AttendanceDailyResponse getDailyAttendance(Long employerId, Long storeId, LocalDate date) {
        Store store = storeAccessService.getMyStore(storeId, employerId);

        List<Schedule> schedules = scheduleRepository.findByStoreAndWorkDate(store, date);
        List<WorkRecord> workRecords = workRecordRepository.findAllByStoreAndWorkDate(storeId, date);
        DailyMetrics metrics = buildDailyMetrics(date, schedules, workRecords);

        return AttendanceDailyResponse.builder()
                .date(date)
                .scheduledCount(metrics.scheduledCount)
                .workedCount(metrics.workedCount)
                .absentCount(metrics.absentCount)
                .unscheduledCount(metrics.unscheduledCount)
                .lateCount(metrics.lateCount)
                .items(metrics.items)
                .build();
    }

    public AttendanceSummaryResponse getSummary(Long employerId, Long storeId, LocalDate from, LocalDate to) {
        validateRange(from, to);

        Store store = storeAccessService.getMyStore(storeId, employerId);

        List<Schedule> schedules = scheduleRepository.findByStoreAndWorkDateBetween(store, from, to);
        List<WorkRecord> workRecords = workRecordRepository.findAllByStoreAndWorkDateBetween(storeId, from, to);

        Map<LocalDate, List<Schedule>> schedulesByDate = new HashMap<>();
        for (Schedule schedule : schedules) {
            schedulesByDate.computeIfAbsent(schedule.getWorkDate(), ignored -> new ArrayList<>()).add(schedule);
        }

        Map<LocalDate, List<WorkRecord>> recordsByDate = new HashMap<>();
        for (WorkRecord workRecord : workRecords) {
            recordsByDate.computeIfAbsent(workRecord.getWorkDate(), ignored -> new ArrayList<>()).add(workRecord);
        }

        int scheduledCount = 0;
        int workedCount = 0;
        int absentCount = 0;
        int unscheduledCount = 0;
        int lateCount = 0;

        LocalDate date = from;
        while (!date.isAfter(to)) {
            DailyMetrics daily = buildDailyMetrics(
                    date,
                    schedulesByDate.getOrDefault(date, List.of()),
                    recordsByDate.getOrDefault(date, List.of())
            );

            scheduledCount += daily.scheduledCount;
            workedCount += daily.workedCount;
            absentCount += daily.absentCount;
            unscheduledCount += daily.unscheduledCount;
            lateCount += daily.lateCount;
            date = date.plusDays(1);
        }

        double attendanceRate = scheduledCount == 0
                ? 0d
                : ((double) (scheduledCount - absentCount) / scheduledCount) * 100;

        return AttendanceSummaryResponse.builder()
                .from(from)
                .to(to)
                .scheduledCount(scheduledCount)
                .workedCount(workedCount)
                .absentCount(absentCount)
                .unscheduledCount(unscheduledCount)
                .lateCount(lateCount)
                .attendanceRate(attendanceRate)
                .build();
    }

    private DailyMetrics buildDailyMetrics(LocalDate date, List<Schedule> schedules, List<WorkRecord> workRecords) {
        // 하루에 여러 타임 근무가 가능하므로 직원당 스케줄을 리스트로 묶는다
        Map<Long, List<Schedule>> schedulesByEmployee = new HashMap<>();
        for (Schedule schedule : schedules) {
            schedulesByEmployee
                    .computeIfAbsent(schedule.getEmployee().getId(), key -> new ArrayList<>())
                    .add(schedule);
        }

        Map<Long, List<WorkRecord>> recordsByEmployee = new HashMap<>();
        for (WorkRecord record : workRecords) {
            recordsByEmployee
                    .computeIfAbsent(record.getEmployee().getId(), key -> new ArrayList<>())
                    .add(record);
        }

        Set<Long> targetEmployeeIds = new HashSet<>();
        targetEmployeeIds.addAll(schedulesByEmployee.keySet());
        targetEmployeeIds.addAll(recordsByEmployee.keySet());

        List<AttendanceDailyEmployeeResponse> items = new ArrayList<>();

        int scheduledCount = 0;
        int workedCount = 0;
        int absentCount = 0;
        int unscheduledCount = 0;
        int lateCount = 0;

        for (Long employeeId : targetEmployeeIds) {
            List<Schedule> employeeSchedules = schedulesByEmployee.getOrDefault(employeeId, List.of());
            List<WorkRecord> records = recordsByEmployee.getOrDefault(employeeId, List.of());

            AttendanceAggregate aggregate = aggregate(records);
            Employee employee = getEmployee(employeeSchedules, records);

            // 스케줄이 여러 개면 가장 이른 시작~가장 늦은 종료를 그날의 근무 예정 구간으로 본다
            LocalDateTime scheduledStart = null;
            LocalDateTime scheduledEnd = null;
            for (Schedule schedule : employeeSchedules) {
                if (scheduledStart == null || schedule.getStartTime().isBefore(scheduledStart)) {
                    scheduledStart = schedule.getStartTime();
                }
                if (scheduledEnd == null || schedule.getEndTime().isAfter(scheduledEnd)) {
                    scheduledEnd = schedule.getEndTime();
                }
            }

            AttendanceMatchStatus status;
            int lateMinutes = 0;
            int earlyLeaveMinutes = 0;

            if (!employeeSchedules.isEmpty() && records.isEmpty()) {
                status = AttendanceMatchStatus.ABSENT;
                scheduledCount++;
                absentCount++;
            } else if (employeeSchedules.isEmpty()) {
                status = AttendanceMatchStatus.UNSCHEDULED;
                workedCount++;
                unscheduledCount++;
            } else {
                scheduledCount++;
                workedCount++;

                if (aggregate.clockOutTime == null) {
                    status = AttendanceMatchStatus.PARTIAL;
                } else {
                    if (aggregate.clockInTime != null && aggregate.clockInTime.isAfter(scheduledStart)) {
                        lateMinutes = (int) ChronoUnit.MINUTES.between(scheduledStart, aggregate.clockInTime);
                    }

                    if (aggregate.clockOutTime.isBefore(scheduledEnd)) {
                        earlyLeaveMinutes = (int) ChronoUnit.MINUTES.between(aggregate.clockOutTime, scheduledEnd);
                    }

                    if (lateMinutes > 0) {
                        status = AttendanceMatchStatus.LATE;
                        lateCount++;
                    } else if (earlyLeaveMinutes > 0) {
                        status = AttendanceMatchStatus.EARLY_LEAVE;
                    } else {
                        status = AttendanceMatchStatus.WORKED;
                    }
                }
            }

            items.add(AttendanceDailyEmployeeResponse.builder()
                    .employeeId(employeeId)
                    .employeeName(employee != null ? employee.getName() : null)
                    .status(status)
                    .scheduledStartTime(scheduledStart)
                    .scheduledEndTime(scheduledEnd)
                    .actualClockInTime(aggregate.clockInTime)
                    .actualClockOutTime(aggregate.clockOutTime)
                    .lateMinutes(lateMinutes)
                    .earlyLeaveMinutes(earlyLeaveMinutes)
                    .workedMinutes(aggregate.workedMinutes)
                    .build());
        }

        items.sort(Comparator.comparing(AttendanceDailyEmployeeResponse::getEmployeeName,
                Comparator.nullsLast(String::compareTo)));

        return new DailyMetrics(date, scheduledCount, workedCount, absentCount, unscheduledCount, lateCount, items);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("조회 시작일/종료일은 필수입니다.");
        }

        if (to.isBefore(from)) {
            throw new IllegalArgumentException("조회 종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    private Employee getEmployee(List<Schedule> schedules, List<WorkRecord> records) {
        if (!schedules.isEmpty()) {
            return schedules.get(0).getEmployee();
        }
        if (!records.isEmpty()) {
            return records.get(0).getEmployee();
        }
        return null;
    }

    private AttendanceAggregate aggregate(List<WorkRecord> records) {
        LocalDateTime firstClockIn = null;
        LocalDateTime lastClockOut = null;
        int workedMinutes = 0;

        for (WorkRecord record : records) {
            if (firstClockIn == null || record.getClockInTime().isBefore(firstClockIn)) {
                firstClockIn = record.getClockInTime();
            }

            if (record.getClockOutTime() != null &&
                    (lastClockOut == null || record.getClockOutTime().isAfter(lastClockOut))) {
                lastClockOut = record.getClockOutTime();
            }

            Long actual = record.getActualWorkMinutes();
            if (actual != null) {
                workedMinutes += actual.intValue();
            }
        }

        return new AttendanceAggregate(firstClockIn, lastClockOut, workedMinutes);
    }

    private record DailyMetrics(
            LocalDate date,
            int scheduledCount,
            int workedCount,
            int absentCount,
            int unscheduledCount,
            int lateCount,
            List<AttendanceDailyEmployeeResponse> items
    ) {
    }

    private record AttendanceAggregate(
            LocalDateTime clockInTime,
            LocalDateTime clockOutTime,
            int workedMinutes
    ) {
    }
}
