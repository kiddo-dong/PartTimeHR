package com.example.PartTimeHR.workrecord.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.service.EmployeeAccessService;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.service.StoreAccessService;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkStatus;
import com.example.PartTimeHR.workrecord.dto.CreateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.mapper.WorkRecordMapper;
import com.example.PartTimeHR.workrecord.repository.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class WorkRecordService {

    private final StoreAccessService storeAccessService;
    private final EmployeeAccessService employeeAccessService;
    private final WorkRecordRepository workRecordRepository;
    private final WorkRecordMapper workRecordMapper;

    /* ======================
       자동 생성 (원클릭)
       ====================== */

    // 출근
    public WorkRecordResponse clockIn(Long employerId, Long storeId, Long employeeId) {

        Employee employee = validateAccess(employerId, storeId, employeeId);

        // 진행 중 근무가 있으면 출근 불가
        if (workRecordRepository.existsByEmployeeAndClockOutTimeIsNull(employee)) {
            throw new IllegalStateException("이미 진행 중인 근무가 존재합니다.");
        }

        WorkRecord record = WorkRecord.builder()
                .employee(employee)
                .workDate(LocalDate.now())
                .clockInTime(LocalDateTime.now())
                .appliedHourlyWage(employee.getPayPolicy().getHourlyWage())
                .appliedJobTitle(employee.getPayPolicy().getJobTitle())
                .status(WorkStatus.IN_PROGRESS)
                .build();

        workRecordRepository.save(record);
        return workRecordMapper.toResponse(record);
    }

    // 휴게 시작
    public WorkRecordResponse startBreak(Long employerId, Long storeId, Long employeeId) {

        WorkRecord record = getActiveRecord(employerId, storeId, employeeId);

        if (!record.canStartBreak()) {
            throw new IllegalStateException("휴게를 시작할 수 없는 상태입니다.");
        }

        record.startBreak();
        return workRecordMapper.toResponse(record);
    }

    // 휴게 종료
    public WorkRecordResponse endBreak(Long employerId, Long storeId, Long employeeId) {

        WorkRecord record = getActiveRecord(employerId, storeId, employeeId);

        if (!record.canEndBreak()) {
            throw new IllegalStateException("휴게 종료가 불가능한 상태입니다.");
        }

        record.endBreak();
        return workRecordMapper.toResponse(record);
    }

    // 퇴근
    public WorkRecordResponse clockOut(Long employerId, Long storeId, Long employeeId) {

        WorkRecord record = getActiveRecord(employerId, storeId, employeeId);

        if (!record.canClockOut()) {
            throw new IllegalStateException("휴게 종료 후 퇴근할 수 있습니다.");
        }

        record.clockOut(LocalDateTime.now());
        return workRecordMapper.toResponse(record);
    }

    /* ======================
       수동 생성
       ====================== */

    public WorkRecordResponse createManual(
            Long employerId,
            Long storeId,
            CreateWorkRecordRequest request
    ) {

        Employee employee = validateAccess(
                employerId, storeId, request.getEmployeeId()
        );

        // 미완료 수동 생성은 전체 기준으로 제한
        if (request.getClockOutTime() == null &&
                workRecordRepository.existsByEmployeeAndClockOutTimeIsNull(employee)) {
            throw new IllegalStateException("이미 진행 중인 근무가 존재합니다.");
        }

        WorkRecord record = WorkRecord.builder()
                .employee(employee)
                .workDate(request.getWorkDate())
                .clockInTime(request.getClockInTime())
                .breakStartTime(request.getBreakStartTime())
                .breakEndTime(request.getBreakEndTime())
                .clockOutTime(request.getClockOutTime())
                .appliedHourlyWage(employee.getPayPolicy().getHourlyWage())
                .appliedJobTitle(employee.getPayPolicy().getJobTitle())
                .status(resolveStatus(request))
                .memo(request.getMemo())
                .build();

        workRecordRepository.save(record);
        return workRecordMapper.toResponse(record);
    }

    /* ======================
       공통 메서드
       ====================== */

    private Employee validateAccess(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        Store store = storeAccessService.getMyStore(storeId, employerId);
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);
        storeAccessService.validateEmployeeInStore(store, employee);
        return employee;
    }

    private WorkRecord getActiveRecord(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        Employee employee = validateAccess(employerId, storeId, employeeId);

        return workRecordRepository
                .findFirstByEmployeeAndClockOutTimeIsNullOrderByClockInTimeDesc(employee)
                .orElseThrow(() ->
                        new IllegalStateException("진행 중인 근무 기록이 없습니다.")
                );
    }

    private WorkStatus resolveStatus(CreateWorkRecordRequest request) {

        if (request.getClockOutTime() != null) {
            return WorkStatus.COMPLETED;
        }

        if (request.getBreakStartTime() != null &&
                request.getBreakEndTime() == null) {
            return WorkStatus.ON_BREAK;
        }

        return WorkStatus.IN_PROGRESS;
    }
}
