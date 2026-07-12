package com.example.PartTimeHR.workrecord.application;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.application.EmployeeAccessService;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.application.StoreAccessService;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkRecordNotFoundException;
import com.example.PartTimeHR.workrecord.domain.WorkRecordRepository;
import com.example.PartTimeHR.workrecord.presentation.dto.CreateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.presentation.dto.UpdateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.presentation.dto.WorkRecordResponse;
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

    public WorkRecordResponse clockIn(Long employerId, Long storeId, Long employeeId) {
        return doClockIn(validateAccess(employerId, storeId, employeeId));
    }

    public WorkRecordResponse startBreak(Long employerId, Long storeId, Long employeeId) {
        WorkRecord record = getActiveRecordOf(validateAccess(employerId, storeId, employeeId));
        record.startBreak(LocalDateTime.now());
        return workRecordMapper.toResponse(record);
    }

    public WorkRecordResponse endBreak(Long employerId, Long storeId, Long employeeId) {
        WorkRecord record = getActiveRecordOf(validateAccess(employerId, storeId, employeeId));
        record.endBreak(LocalDateTime.now());
        return workRecordMapper.toResponse(record);
    }

    public WorkRecordResponse clockOut(Long employerId, Long storeId, Long employeeId) {
        WorkRecord record = getActiveRecordOf(validateAccess(employerId, storeId, employeeId));
        record.clockOut(LocalDateTime.now());
        return workRecordMapper.toResponse(record);
    }

    /* ======================
       원클릭 (출근 / 휴게 / 퇴근) - 직원 본인
       ====================== */

    public WorkRecordResponse clockInSelf(Long employeeId) {
        return doClockIn(employeeAccessService.getEmployeeOrThrow(employeeId));
    }

    public WorkRecordResponse startBreakSelf(Long employeeId) {
        WorkRecord record = getActiveRecordOf(employeeAccessService.getEmployeeOrThrow(employeeId));
        record.startBreak(LocalDateTime.now());
        return workRecordMapper.toResponse(record);
    }

    public WorkRecordResponse endBreakSelf(Long employeeId) {
        WorkRecord record = getActiveRecordOf(employeeAccessService.getEmployeeOrThrow(employeeId));
        record.endBreak(LocalDateTime.now());
        return workRecordMapper.toResponse(record);
    }

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
        workRecordRepository
                .findFirstByEmployeeAndClockOutTimeIsNullOrderByClockInTimeDesc(employee)
                .ifPresent(openRecord -> {
                    // 오늘 진행 중인 근무는 중복 출근으로 차단
                    if (openRecord.getWorkDate().isEqual(LocalDate.now())) {
                        throw new IllegalStateException("이미 진행 중인 근무가 존재합니다.");
                    }
                    // 이전 날짜의 미퇴근 근무는 자동 마감하고 새 출근을 허용
                    openRecord.autoClose();
                });

        WorkRecord record = WorkRecord.builder()
                .employee(employee)
                .workDate(LocalDate.now())
                .clockInTime(LocalDateTime.now())
                .appliedHourlyWage(employee.getPayPolicy().getHourlyWage())
                .appliedJobTitle(employee.getPayPolicy().getJobTitle())
                .build();

        workRecordRepository.save(record);
        return workRecordMapper.toResponse(record);
    }

    /* ======================
       수동 생성/수정/삭제 (관리자 입력)
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
                .clockOutTime(request.getClockOutTime())
                .appliedHourlyWage(employee.getPayPolicy().getHourlyWage())
                .appliedJobTitle(employee.getPayPolicy().getJobTitle())
                .memo(request.getMemo())
                .build();

        record.replaceBreaks(request.getBreakStartTime(), request.getBreakEndTime());
        record.validateTimes();

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

        // 부분 수정 (null 필드는 기존 값 유지)
        record.updateManually(
                request.getClockInTime(),
                request.getClockOutTime(),
                request.getMemo()
        );

        // 휴게 필드를 보낸 경우에만 휴게 목록을 입력된 쌍으로 교체
        if (request.getBreakStartTime() != null || request.getBreakEndTime() != null) {
            record.replaceBreaks(request.getBreakStartTime(), request.getBreakEndTime());
        }

        record.validateTimes();

        // dirty checking으로 자동 저장
        return workRecordMapper.toResponse(record);
    }

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
                        new WorkRecordNotFoundException("해당 근무 기록이 존재하지 않습니다.")
                );

        // 해당 근무 기록이 이 가게 소속인지 검증
        storeAccessService.validateEmployeeInStore(store, record.getEmployee());

        // 삭제 (휴게는 cascade로 함께 삭제)
        workRecordRepository.delete(record);
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
}
