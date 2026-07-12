package com.example.PartTimeHR.workrecord.application;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.application.EmployeeAccessService;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.application.StoreAccessService;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkStatus;
import com.example.PartTimeHR.workrecord.presentation.dto.CreateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.presentation.dto.UpdateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.presentation.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.application.WorkRecordMapper;
import com.example.PartTimeHR.workrecord.domain.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkRecordService {

    private final StoreAccessService storeAccessService;
    private final EmployeeAccessService employeeAccessService;
    private final WorkRecordRepository workRecordRepository;
    private final WorkRecordMapper workRecordMapper;

    /* ======================
       자동 생성 (출근 / 휴게 / 퇴근)
       ====================== */

    // 출근
    public WorkRecordResponse clockIn(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        Employee employee = validateAccess(employerId, storeId, employeeId);

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
                .totalBreakMinutes(0)
                .totalWorkedMinutes(0)
                .netWorkedMinutes(0)
                .build();

        workRecordRepository.save(record);
        return workRecordMapper.toResponse(record);
    }

    // 휴게 시작
    public WorkRecordResponse startBreak(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        WorkRecord record = getActiveRecord(employerId, storeId, employeeId);
        record.startBreak();
        return workRecordMapper.toResponse(record);
    }

    // 휴게 종료
    public WorkRecordResponse endBreak(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        WorkRecord record = getActiveRecord(employerId, storeId, employeeId);
        record.endBreak();
        return workRecordMapper.toResponse(record);
    }

    // 퇴근
    public WorkRecordResponse clockOut(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        WorkRecord record = getActiveRecord(employerId, storeId, employeeId);
        record.clockOut(LocalDateTime.now());
        return workRecordMapper.toResponse(record);
    }

    /* ======================
       수동 생성 (관리자 입력)
       ====================== */

    public WorkRecordResponse createManual(
            Long employerId,
            Long storeId,
            CreateWorkRecordRequest request
    ) {
        Employee employee = validateAccess(
                employerId, storeId, request.getEmployeeId()
        );

        // 진행 중 근무 중복 방지
        if (request.getClockOutTime() == null &&
                workRecordRepository.existsByEmployeeAndClockOutTimeIsNull(employee)) {
            throw new IllegalStateException("이미 진행 중인 근무가 존재합니다.");
        }

        WorkStatus status = resolveStatus(request);

        WorkRecord record = WorkRecord.builder()
                .employee(employee)
                .workDate(request.getWorkDate())
                .clockInTime(request.getClockInTime())
                .breakStartTime(request.getBreakStartTime())
                .breakEndTime(request.getBreakEndTime())
                .clockOutTime(request.getClockOutTime())
                .appliedHourlyWage(employee.getPayPolicy().getHourlyWage())
                .appliedJobTitle(employee.getPayPolicy().getJobTitle())
                .status(status)
                .memo(request.getMemo())
                .totalBreakMinutes(0)
                .totalWorkedMinutes(0)
                .netWorkedMinutes(0)
                .build();

        workRecordRepository.save(record);
        return workRecordMapper.toResponse(record);
    }
    @Transactional
    public WorkRecordResponse updateWorkRecord(Long employerId, Long storeId, Long workRecordId, UpdateWorkRecordRequest request) {
        // 1. 가게 소유 확인
        storeAccessService.getMyStore(storeId, employerId);

        // 2. 기존 근무 기록 조회
        WorkRecord record = workRecordRepository.findById(workRecordId)
                .orElseThrow(() -> new IllegalArgumentException("해당 근무 기록이 존재하지 않습니다."));

        // 3. MapStruct로 request 덮어쓰기
        workRecordMapper.updateFromRequest(request, record);

        // 4. dirty checking으로 자동 저장
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

    // 삭제
    @Transactional
    public void deleteWorkRecord(
            Long employerId,
            Long storeId,
            Long workRecordId
    ) {
        // 가게 소유 확인 (사장 권한 검증)
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 근무 기록 조회
        WorkRecord record = workRecordRepository.findById(workRecordId)
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 근무 기록이 존재하지 않습니다.")
                );

        // 해당 근무 기록이 이 가게 소속인지 검증
        Employee employee = record.getEmployee();
        storeAccessService.validateEmployeeInStore(store, employee);

        // 삭제
        workRecordRepository.delete(record);
    }
}
