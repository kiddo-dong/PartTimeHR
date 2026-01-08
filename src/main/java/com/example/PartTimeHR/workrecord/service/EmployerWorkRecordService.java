package com.example.PartTimeHR.workrecord.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.workrecord.exception.WorkRecordNotFoundException;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkStatus;
import com.example.PartTimeHR.workrecord.dto.CreateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.UpdateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.mapper.WorkRecordMapper;
import com.example.PartTimeHR.workrecord.repository.EmployerWorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployerWorkRecordService {

    private final EmployerWorkRecordRepository employerWorkRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployerRepository employerRepository;
    private final WorkRecordMapper workRecordMapper;

    // ====== 생성/수정/삭제 ======
    // 수동 등록
    @Transactional
    public WorkRecordResponse createWorkRecord(Long employerId, CreateWorkRecordRequest request) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));

        // 자신의 직원인지 확인
        if (!employee.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원에게만 출근 기록을 등록할 수 있습니다.");
        }

        // 시간 순서 검증
        validateTimeOrder(request.getClockInTime(), request.getBreakStartTime(),
                request.getBreakEndTime(), request.getClockOutTime());

        // 상태 결정
        WorkStatus status = determineStatus(request.getClockOutTime(), request.getBreakStartTime(), request.getBreakEndTime());

        WorkRecord workRecord = WorkRecord.builder()
                .employee(employee)
                .workDate(request.getWorkDate())
                .clockInTime(request.getClockInTime())
                .breakStartTime(request.getBreakStartTime())
                .breakEndTime(request.getBreakEndTime())
                .clockOutTime(request.getClockOutTime())
                .status(status)
                .memo(request.getMemo())
                .appliedHourlyWage(employee.getPayPolicy().getHourlyWage())
                .appliedJobName(employee.getPayPolicy().getJobTitle())
                .build();

        employerWorkRecordRepository.save(workRecord);

        return workRecordMapper.toResponse(workRecord);
    }

    // 수정
    @Transactional
    public WorkRecordResponse updateWorkRecord(Long recordId, Long employerId, UpdateWorkRecordRequest request) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        WorkRecord workRecord = employerWorkRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("출근 기록을 찾을 수 없습니다."));

        // 자신의 직원의 기록인지 확인
        if (!workRecord.getEmployee().getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원의 출근 기록만 수정할 수 있습니다.");
        }

        // 부분 수정 (null이 아닌 필드만 업데이트)
        if (request.getClockInTime() != null) {
            workRecord.setClockInTime(request.getClockInTime());

            // 날짜 자동 설정 (출근일 기준)
            workRecord.setWorkDate(request.getClockInTime().toLocalDate());
        }
        if (request.getBreakStartTime() != null) {
            workRecord.setBreakStartTime(request.getBreakStartTime());
        }
        if (request.getBreakEndTime() != null) {
            workRecord.setBreakEndTime(request.getBreakEndTime());
        }
        if (request.getClockOutTime() != null) {
            workRecord.setClockOutTime(request.getClockOutTime());
        }
        if (request.getMemo() != null) {
            workRecord.setMemo(request.getMemo());
        }

        // 시간 순서 검증
        validateTimeOrder(
                workRecord.getClockInTime(),
                workRecord.getBreakStartTime(),
                workRecord.getBreakEndTime(),
                workRecord.getClockOutTime()
        );

        // 상태 자동 업데이트
        WorkStatus newStatus = determineStatus(
                workRecord.getClockOutTime(),
                workRecord.getBreakStartTime(),
                workRecord.getBreakEndTime()
        );
        workRecord.setStatus(newStatus);

        employerWorkRecordRepository.save(workRecord);

        return workRecordMapper.toResponse(workRecord);
    }

    // 삭제
    @Transactional
    public void deleteWorkRecord(Long recordId, String email) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        WorkRecord workRecord = employerWorkRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("출근 기록을 찾을 수 없습니다."));

        // 자신의 직원의 기록인지 확인
        if (!workRecord.getEmployee().getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원의 출근 기록만 삭제할 수 있습니다.");
        }

        employerWorkRecordRepository.delete(workRecord);
    }

    // ====== 생성/수정/삭제 헬퍼 메소드 ======
    // 시간 순서 검증
    private void validateTimeOrder(LocalDateTime clockIn, LocalDateTime breakStart,
                                   LocalDateTime breakEnd, LocalDateTime clockOut) {
        if (clockIn == null) {
            return; // 출근 시간은 필수이므로 여기 도달하면 안 됨
        }

        if (breakStart != null) {
            if (!clockIn.isBefore(breakStart)) {
                throw new IllegalArgumentException("출근 시간은 휴게 시작 시간보다 이전이어야 합니다.");
            }
        }

        if (breakStart != null && breakEnd != null) {
            if (!breakStart.isBefore(breakEnd)) {
                throw new IllegalArgumentException("휴게 시작 시간은 휴게 끝 시간보다 이전이어야 합니다.");
            }
        }

        if (breakEnd != null && clockOut != null) {
            if (!breakEnd.isBefore(clockOut)) {
                throw new IllegalArgumentException("휴게 끝 시간은 퇴근 시간보다 이전이어야 합니다.");
            }
        }

        if (clockOut != null) {
            if (!clockIn.isBefore(clockOut)) {
                throw new IllegalArgumentException("출근 시간은 퇴근 시간보다 이전이어야 합니다.");
            }
        }
    }

    // 상태 결정
    private WorkStatus determineStatus(LocalDateTime clockOut, LocalDateTime breakStart, LocalDateTime breakEnd) {
        if (clockOut != null) {
            return WorkStatus.COMPLETED;
        } else if (breakStart != null && breakEnd == null) {
            return WorkStatus.ON_BREAK;
        } else {
            return WorkStatus.IN_PROGRESS;
        }
    }


    // =============== 기록 조회 ===============

    // 당일 전체
    public List<WorkRecordResponse> todayWorkRecords() {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<WorkRecord> workRecordList = employerWorkRecordRepository.findByToday(today);

        if (workRecordList.isEmpty()) {
            throw new WorkRecordNotFoundException("오늘 출근한 직원이 존재하지 않습니다.");
        }

        return workRecordMapper.toResponseList(workRecordList);
    }

    // 가게 전체 - 주간
    public List<WorkRecordResponse> findStoreWeek(
            Long employerId,
            int offset
    ) {
        Integer weekStartDay = employerRepository.findWeekStartDay(employerId);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate start = calculateWeekStart(today, weekStartDay).plusWeeks(offset);
        LocalDate end = start.plusDays(6);

        List<WorkRecord> workRecordList = employerWorkRecordRepository.findByEmployerAndPeriod(employerId, start, end);

        if (workRecordList.isEmpty()) {
            throw new WorkRecordNotFoundException("해당 기간의 근무 기록이 존재하지 않습니다.");
        }

        return workRecordMapper.toResponseList(workRecordList);

    }

    // 가게 전체 - 월간
    public List<WorkRecordResponse> findStoreMonth(
            Long employerId,
            int offset
    ) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate targetMonth = today.plusMonths(offset);

        LocalDate start = targetMonth.withDayOfMonth(1);
        LocalDate end = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

        List<WorkRecord> workRecordList = employerWorkRecordRepository.findByEmployerAndPeriod(employerId, start, end);

        if (workRecordList.isEmpty()) {
            throw new WorkRecordNotFoundException("해당 기간의 근무 기록이 존재하지 않습니다.");
        }

        return workRecordMapper.toResponseList(workRecordList);
    }

    // 특정 직원 - 주간
    public List<WorkRecordResponse> findEmployeeWeek(
            Long employerId,
            Long employeeId,
            int offset
    ) {
        // 접근 권한 확인
        validateEmployee(employerId, employeeId);

        Integer weekStartDay = employerRepository.findWeekStartDay(employerId);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate start = calculateWeekStart(today, weekStartDay).plusWeeks(offset);
        LocalDate end = start.plusDays(6);

        List<WorkRecord> workRecordList = employerWorkRecordRepository.findByEmployeeIdAndWorkDateBetween(employeeId, start, end);

        if (workRecordList.isEmpty()) {
            throw new WorkRecordNotFoundException("해당 기간의 근무 기록이 존재하지 않습니다.");
        }

        return workRecordMapper.toResponseList(workRecordList);
    }

    // 특정 직원 - 월간
    public List<WorkRecordResponse> findEmployeeMonth(
            Long employerId,
            Long employeeId,
            int offset
    ) {
        validateEmployee(employerId, employeeId);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate targetMonth = today.plusMonths(offset);

        LocalDate start = targetMonth.withDayOfMonth(1);
        LocalDate end = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

        List<WorkRecord> workRecordList = employerWorkRecordRepository.findByEmployeeIdAndWorkDateBetween(employeeId, start, end);

        if (workRecordList.isEmpty()) {
            throw new WorkRecordNotFoundException("해당 기간의 근무 기록이 존재하지 않습니다.");
        }

        return workRecordMapper.toResponseList(workRecordList);
    }

    //   기간 조회 (공통)
    public List<WorkRecordResponse> findByPeriod(
            Long employerId,
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateEmployee(employerId, employeeId);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이후일 수 없습니다.");
        }

        List<WorkRecord> workRecordList = employerWorkRecordRepository.findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate);

        if (workRecordList.isEmpty()) {
            throw new WorkRecordNotFoundException("해당 기간의 근무 기록이 존재하지 않습니다.");
        }

        return workRecordMapper.toResponseList(workRecordList);
    }

    // ===== 유틸 메소드 =====
    private void validateEmployee(Long employerId, Long employeeId) {
        if (!employeeRepository.existsByIdAndEmployerId(employeeId, employerId)) {
            throw new AccessDeniedException("해당 직원에 대한 접근 권한이 없습니다.");
        }
    }

    private LocalDate calculateWeekStart(LocalDate today, int weekStartDay) {
        DayOfWeek startDay = DayOfWeek.of(weekStartDay);
        return today.with(TemporalAdjusters.previousOrSame(startDay));
    }
}


