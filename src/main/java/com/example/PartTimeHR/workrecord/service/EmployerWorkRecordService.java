package com.example.PartTimeHR.workrecord.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkStatus;
import com.example.PartTimeHR.workrecord.dto.CreateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.EmployeeClockInRequest;
import com.example.PartTimeHR.workrecord.dto.UpdateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.mapper.WorkRecordMapper;
import com.example.PartTimeHR.workrecord.repository.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployerWorkRecordService {

    private final WorkRecordRepository workRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployerRepository employerRepository;
    private final WorkRecordMapper workRecordMapper;
    private final PasswordEncoder passwordEncoder;

    // ==================== 고용주용 메서드 ====================

    // 수동 등록
    @Transactional
    public WorkRecordResponse createWorkRecord(String email, CreateWorkRecordRequest request) {
        Employer employer = employerRepository.findByEmail(email)
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

        workRecordRepository.save(workRecord);

        return workRecordMapper.toResponse(workRecord);
    }

    // 수정
    @Transactional
    public WorkRecordResponse updateWorkRecord(Long recordId, String email, UpdateWorkRecordRequest request) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        WorkRecord workRecord = workRecordRepository.findById(recordId)
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

        workRecordRepository.save(workRecord);

        return workRecordMapper.toResponse(workRecord);
    }

    // 삭제
    @Transactional
    public void deleteWorkRecord(Long recordId, String email) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        WorkRecord workRecord = workRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("출근 기록을 찾을 수 없습니다."));

        // 자신의 직원의 기록인지 확인
        if (!workRecord.getEmployee().getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원의 출근 기록만 삭제할 수 있습니다.");
        }

        workRecordRepository.delete(workRecord);
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<WorkRecordResponse> getAllRecords(String email, Long employeeId, LocalDate startDate, LocalDate endDate) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        List<WorkRecord> records;

        if (employeeId != null) {
            // 특정 직원의 기록
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));

            // 자신의 직원인지 확인
            if (!employee.getEmployer().getId().equals(employer.getId())) {
                throw new IllegalArgumentException("자신의 직원의 기록만 조회할 수 있습니다.");
            }

            if (startDate != null && endDate != null) {
                records = workRecordRepository.findByEmployeeAndWorkDateBetween(employee, startDate, endDate);
            } else {
                records = workRecordRepository.findByEmployeeOrderByWorkDateDesc(employee);
            }
        } else {
            // 모든 직원의 기록 (자신의 직원들만)
            List<Employee> employees = employeeRepository.findByEmployer(employer);

            if (startDate != null && endDate != null) {
                records = employees.stream()
                        .flatMap(emp -> workRecordRepository.findByEmployeeAndWorkDateBetween(emp, startDate, endDate).stream())
                        .toList();
            } else {
                records = employees.stream()
                        .flatMap(emp -> workRecordRepository.findByEmployeeOrderByWorkDateDesc(emp).stream())
                        .toList();
            }
        }

        return records.stream()
                .map(workRecordMapper::toResponse)
                .toList();
    }

    // 특정 기록 조회
    @Transactional(readOnly = true)
    public WorkRecordResponse getWorkRecord(Long recordId, String email) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        WorkRecord workRecord = workRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("출근 기록을 찾을 수 없습니다."));

        // 자신의 직원의 기록인지 확인
        if (!workRecord.getEmployee().getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원의 출근 기록만 조회할 수 있습니다.");
        }

        return workRecordMapper.toResponse(workRecord);
    }

    // ==================== Private Helper Methods ====================

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
}

