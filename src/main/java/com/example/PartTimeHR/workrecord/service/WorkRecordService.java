package com.example.PartTimeHR.workrecord.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.service.EmployeeAccessService;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.service.StoreAccessService;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkStatus;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.mapper.WorkRecordMapper;
import com.example.PartTimeHR.workrecord.repository.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkRecordService {

    private final StoreAccessService storeAccessService;
    private final EmployeeAccessService employeeAccessService;
    private final WorkRecordRepository workRecordRepository;
    private final WorkRecordMapper workRecordMapper;

    // 출근
    public WorkRecordResponse clockIn(Long employerId, Long storeId, Long employeeId) {
        Store store = storeAccessService.getMyStore(storeId, employerId);
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);
        storeAccessService.validateEmployeeInStore(store, employee);

        LocalDate today = LocalDate.now();

        // 오늘 미완료 기록이 있으면 출근 불가
        List<WorkRecord> todayRecords =
                workRecordRepository.findByEmployeeAndWorkDate(employee, today);

        boolean hasActiveRecord = todayRecords.stream()
                .anyMatch(r -> r.getStatus() != WorkStatus.COMPLETED);

        if (hasActiveRecord) {
            throw new IllegalStateException("오늘 아직 퇴근하지 않은 출근 기록이 있습니다.");
        }

        // 6. 생성
        WorkRecord record = WorkRecord.builder()
                .employee(employee)
                .workDate(today)
                .clockInTime(java.time.LocalDateTime.now())
                .appliedHourlyWage(employee.getPayPolicy().getHourlyWage())
                .appliedJobTitle(employee.getPayPolicy().getJobTitle())
                .status(WorkStatus.IN_PROGRESS)
                .build();

        workRecordRepository.save(record);

        return workRecordMapper.toResponse(record);
    }

    // 휴게 시작
    public WorkRecordResponse startBreak(Long employerId, Long storeId, Long employeeId) {

        WorkRecord record = getTodayActiveRecord(employerId, storeId, employeeId);

        if (record.getStatus() != WorkStatus.IN_PROGRESS) {
            throw new IllegalStateException("근무 중일 때만 휴게를 시작할 수 있습니다.");
        }

        record.startBreak();
        return workRecordMapper.toResponse(record);
    }

    // 휴게 종료
    public WorkRecordResponse endBreak(Long employerId, Long storeId, Long employeeId) {

        WorkRecord record = getTodayActiveRecord(employerId, storeId, employeeId);

        if (record.getStatus() != WorkStatus.ON_BREAK) {
            throw new IllegalStateException("휴게 중일 때만 종료할 수 있습니다.");
        }

        record.endBreak();
        return workRecordMapper.toResponse(record);
    }

    // 퇴근
    public WorkRecordResponse clockOut(Long employerId, Long storeId, Long employeeId) {

        WorkRecord record = getTodayActiveRecord(employerId, storeId, employeeId);

        if (record.getStatus() == WorkStatus.COMPLETED) {
            throw new IllegalStateException("이미 퇴근 처리된 기록입니다.");
        }

        record.clockOut();
        return workRecordMapper.toResponse(record);
    }

    // 공통 로직 (당일 출근 기록 조회 및 해당 기록 기반 )
    private WorkRecord getTodayActiveRecord(Long employerId, Long storeId, Long employeeId) {

        Store store = storeAccessService.getMyStore(storeId, employerId);
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);
        storeAccessService.validateEmployeeInStore(store, employee);

        return workRecordRepository
                .findTopByEmployeeAndWorkDateAndStatusNotOrderByClockInTimeDesc(
                        employee,
                        LocalDate.now(),
                        WorkStatus.COMPLETED
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("오늘 진행 중인 출근 기록이 없습니다.")
                );
    }

}