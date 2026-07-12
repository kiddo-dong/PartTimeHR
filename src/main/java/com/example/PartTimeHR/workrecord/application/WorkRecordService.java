package com.example.PartTimeHR.workrecord.application;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.application.EmployeeAccessService;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.application.StoreAccessService;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkRecordNotFoundException;
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
       원클릭 (출근 / 휴게 / 퇴근) - 사장 컨텍스트
       ====================== */

    // 출근
    public WorkRecordResponse clockIn(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        return doClockIn(validateAccess(employerId, storeId, employeeId));
    }

    // 휴게 시작
    public WorkRecordResponse startBreak(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        WorkRecord record = getActiveRecordOf(validateAccess(employerId, storeId, employeeId));
        record.startBreak();
        return workRecordMapper.toResponse(record);
    }

    // 휴게 종료
    public WorkRecordResponse endBreak(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        WorkRecord record = getActiveRecordOf(validateAccess(employerId, storeId, employeeId));
        record.endBreak();
        return workRecordMapper.toResponse(record);
    }

    // 퇴근
    public WorkRecordResponse clockOut(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        WorkRecord record = getActiveRecordOf(validateAccess(employerId, storeId, employeeId));
        record.clockOut(LocalDateTime.now());
        return workRecordMapper.toResponse(record);
    }

    /* ======================
       원클릭 (출근 / 휴게 / 퇴근) - 직원 본인
       ====================== */

    // 출근 (본인)
    public WorkRecordResponse clockInSelf(Long employeeId) {
        return doClockIn(employeeAccessService.getEmployeeOrThrow(employeeId));
    }

    // 휴게 시작 (본인)
    public WorkRecordResponse startBreakSelf(Long employeeId) {
        WorkRecord record = getActiveRecordOf(employeeAccessService.getEmployeeOrThrow(employeeId));
        record.startBreak();
        return workRecordMapper.toResponse(record);
    }

    // 휴게 종료 (본인)
    public WorkRecordResponse endBreakSelf(Long employeeId) {
        WorkRecord record = getActiveRecordOf(employeeAccessService.getEmployeeOrThrow(employeeId));
        record.endBreak();
        return workRecordMapper.toResponse(record);
    }

    // 퇴근 (본인)
    public WorkRecordResponse clockOutSelf(Long employeeId) {
        WorkRecord record = getActiveRecordOf(employeeAccessService.getEmployeeOrThrow(employeeId));
        record.clockOut(LocalDateTime.now());
        return workRecordMapper.toResponse(record);
    }

    // 당일 근무 기록 조회 (본인)
    @Transactional(readOnly = true)
    public List<WorkRecordResponse> getMyTodayRecords(Long employeeId) {
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        return workRecordRepository
                .findAllByEmployeeAndWorkDate(employee, LocalDate.now())
                .stream()
                .map(workRecordMapper::toResponse)
                .toList();
    }

    // 출근 처리 공통 로직
    private WorkRecordResponse doClockIn(Employee employee) {
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

        WorkRecord record = WorkRecord.builder()
                .employee(employee)
                .workDate(request.getWorkDate())
                .clockInTime(request.getClockInTime())
                .breakStartTime(request.getBreakStartTime())
                .breakEndTime(request.getBreakEndTime())
                .clockOutTime(request.getClockOutTime())
                .appliedHourlyWage(employee.getPayPolicy().getHourlyWage())
                .appliedJobTitle(employee.getPayPolicy().getJobTitle())
                .status(WorkStatus.IN_PROGRESS)
                .memo(request.getMemo())
                .totalBreakMinutes(0)
                .totalWorkedMinutes(0)
                .netWorkedMinutes(0)
                .build();

        // 시간 순서 검증 → 상태 도출 → 누적 휴게 반영 → 집계 확정
        record.validateTimes();
        record.refreshStatus();
        record.applyBreakFromTimes();
        record.recalculateMinutes();

        workRecordRepository.save(record);
        return workRecordMapper.toResponse(record);
    }

    @Transactional
    public WorkRecordResponse updateWorkRecord(Long employerId, Long storeId, Long workRecordId, UpdateWorkRecordRequest request) {
        // 가게 소유 확인
        Store store = storeAccessService.getMyStore(storeId, employerId);

        WorkRecord record = workRecordRepository.findById(workRecordId)
                .orElseThrow(() -> new WorkRecordNotFoundException("해당 근무 기록이 존재하지 않습니다."));

        // 이 가게 소속 직원의 기록인지 검증
        storeAccessService.validateEmployeeInStore(store, record.getEmployee());

        // MapStruct로 request 덮어쓰기 (부분 수정)
        workRecordMapper.updateFromRequest(request, record);

        // 수정 결과의 시간 순서 검증 + 상태 재도출
        record.validateTimes();
        record.refreshStatus();

        // 휴게 시간이 수정된 경우에만 누적 휴게를 입력된 쌍으로 덮어쓴다
        // (다회 휴게가 누적된 기록에서 다른 필드만 고칠 때 누적값 보존)
        if (request.getBreakStartTime() != null || request.getBreakEndTime() != null) {
            record.applyBreakFromTimes();
        }

        // 시간이 바뀌었으므로 집계 재계산
        record.recalculateMinutes();

        // dirty checking으로 자동 저장
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

    // 진행 중인 근무 기록 조회
    private WorkRecord getActiveRecordOf(Employee employee) {
        return workRecordRepository
                .findFirstByEmployeeAndClockOutTimeIsNullOrderByClockInTimeDesc(employee)
                .orElseThrow(() ->
                        new IllegalStateException("진행 중인 근무 기록이 없습니다.")
                );
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
