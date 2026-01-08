package com.example.PartTimeHR.workrecord.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkStatus;
import com.example.PartTimeHR.workrecord.dto.EmployeeClockInRequest;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.mapper.WorkRecordMapper;
import com.example.PartTimeHR.workrecord.repository.EmployeeWorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeWorkRecordActionService {

    private final EmployeeWorkRecordRepository employeeWorkRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployerRepository employerRepository;
    private final WorkRecordMapper workRecordMapper;
    private final PasswordEncoder passwordEncoder;

    // ==================== 사장님 로그인 상태에서 직원 출근/퇴근 ====================

    // 사장님 로그인 상태에서 직원 출근하기 (직원이 이메일/비밀번호 입력)
    @Transactional
    public WorkRecordResponse clockInByEmployer(String email, EmployeeClockInRequest request) {
        // 사장님 확인
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        // 직원 인증
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));

        // 자신의 직원인지 확인
        if (!employee.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원만 출근할 수 있습니다.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 오늘의 출근 기록 확인 (아직 퇴근하지 않은 기록이 있는지 체크)
        LocalDate today = LocalDate.now();
        List<WorkRecord> todayRecords = employeeWorkRecordRepository.findByEmployeeAndWorkDateBetween(
                employee, today, today
        );

        // 아직 퇴근하지 않은 기록이 있는지 확인 (실수로 두 번 출근 방지)
        boolean hasIncompleteRecord = todayRecords.stream()
                .anyMatch(record -> record.getStatus() != WorkStatus.COMPLETED);

        if (hasIncompleteRecord) {
            throw new IllegalArgumentException("오늘 아직 퇴근하지 않은 출근 기록이 있습니다. 먼저 퇴근해주세요.");
        }

        // 출근 기록 생성
        LocalDateTime now = LocalDateTime.now();

        WorkRecord workRecord = WorkRecord.builder()
                .employee(employee)
                .workDate(today)
                .clockInTime(now)
                .status(WorkStatus.IN_PROGRESS)
                .appliedHourlyWage(employee.getPayPolicy().getHourlyWage())
                .appliedJobName(employee.getPayPolicy().getJobTitle())
                .build();

        employeeWorkRecordRepository.save(workRecord);

        return workRecordMapper.toResponse(workRecord);
    }

    // 사장님 로그인 상태에서 직원 휴게 시작 (record_id 없이 자동으로 오늘의 가장 최근 기록 찾기)
    @Transactional
    public WorkRecordResponse startBreakByEmployer(String email, EmployeeClockInRequest request) {
        // 사장님 확인
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        // 직원 인증
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));

        // 자신의 직원인지 확인
        if (!employee.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원만 수정할 수 있습니다.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 오늘의 가장 최근 기록 찾기
        LocalDate today = LocalDate.now();
        List<WorkRecord> todayRecords = employeeWorkRecordRepository.findByEmployeeAndWorkDateBetween(
                employee, today, today
        );

        if (todayRecords.isEmpty()) {
            throw new IllegalArgumentException("오늘 출근 기록이 없습니다. 먼저 출근해주세요.");
        }

        WorkRecord workRecord = todayRecords.stream()
                .max(Comparator.comparing(WorkRecord::getClockInTime))
                .orElseThrow(() -> new IllegalArgumentException("출근 기록을 찾을 수 없습니다."));

        // 상태 확인
        if (workRecord.getStatus() != WorkStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("근무 중일 때만 휴게를 시작할 수 있습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        workRecord.setBreakStartTime(now);
        workRecord.setStatus(WorkStatus.ON_BREAK);

        employeeWorkRecordRepository.save(workRecord);

        return workRecordMapper.toResponse(workRecord);
    }

    // 사장님 로그인 상태에서 직원 휴게 끝 (record_id 없이 자동으로 오늘의 가장 최근 기록 찾기)
    @Transactional
    public WorkRecordResponse endBreakByEmployer(String email, EmployeeClockInRequest request) {
        // 사장님 확인
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        // 직원 인증
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));

        // 자신의 직원인지 확인
        if (!employee.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원만 수정할 수 있습니다.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 오늘의 가장 최근 기록 찾기
        LocalDate today = LocalDate.now();
        List<WorkRecord> todayRecords = employeeWorkRecordRepository.findByEmployeeAndWorkDateBetween(
                employee, today, today
        );

        if (todayRecords.isEmpty()) {
            throw new IllegalArgumentException("오늘 출근 기록이 없습니다. 먼저 출근해주세요.");
        }

        WorkRecord workRecord = todayRecords.stream()
                .max(Comparator.comparing(WorkRecord::getClockInTime))
                .orElseThrow(() -> new IllegalArgumentException("출근 기록을 찾을 수 없습니다."));

        // 상태 확인
        if (workRecord.getStatus() != WorkStatus.ON_BREAK) {
            throw new IllegalArgumentException("휴게 중일 때만 휴게를 종료할 수 있습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        workRecord.setBreakEndTime(now);
        workRecord.setStatus(WorkStatus.IN_PROGRESS);

        employeeWorkRecordRepository.save(workRecord);

        return workRecordMapper.toResponse(workRecord);
    }

    // 사장님 로그인 상태에서 직원 퇴근하기 (record_id 없이 자동으로 오늘의 가장 최근 기록 찾기)
    @Transactional
    public WorkRecordResponse clockOutByEmployer(String email, EmployeeClockInRequest request) {
        // 사장님 확인
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        // 직원 인증
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));

        // 자신의 직원인지 확인
        if (!employee.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원만 수정할 수 있습니다.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 오늘의 가장 최근 기록 찾기
        LocalDate today = LocalDate.now();
        List<WorkRecord> todayRecords = employeeWorkRecordRepository.findByEmployeeAndWorkDateBetween(
                employee, today, today
        );

        if (todayRecords.isEmpty()) {
            throw new IllegalArgumentException("오늘 출근 기록이 없습니다. 먼저 출근해주세요.");
        }

        WorkRecord workRecord = todayRecords.stream()
                .max(Comparator.comparing(WorkRecord::getClockInTime))
                .orElseThrow(() -> new IllegalArgumentException("출근 기록을 찾을 수 없습니다."));

        // 상태 확인
        if (workRecord.getStatus() == WorkStatus.COMPLETED) {
            throw new IllegalArgumentException("이미 퇴근했습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        workRecord.setClockOutTime(now);
        workRecord.setStatus(WorkStatus.COMPLETED);

        employeeWorkRecordRepository.save(workRecord);

        return workRecordMapper.toResponse(workRecord);
    }
}
