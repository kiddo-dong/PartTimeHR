package com.example.PartTimeHR.workrecord.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.exception.EmployeeNotFoundException;
import com.example.PartTimeHR.workrecord.exception.WorkRecordNotFoundException;
import com.example.PartTimeHR.workrecord.mapper.WorkRecordMapper;
import com.example.PartTimeHR.workrecord.repository.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeWorkRecordQueryService {

    private final WorkRecordRepository workRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkRecordMapper workRecordMapper;

    // 오늘 근무 기록 조회
    @Transactional(readOnly = true)
    public List<WorkRecordResponse> today(Long userId) {

        Employee employee = employeeRepository.findById(userId)
                .orElseThrow(EmployeeNotFoundException::new);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<WorkRecord> workRecordList = workRecordRepository.findByEmployeeAndWorkDate(employee, today);

        if (workRecordList.isEmpty()) {
            throw new WorkRecordNotFoundException("오늘 출근한 기록이 없습니다.");
        }

        return workRecordMapper.toResponseList(workRecordList);
    }

    // 특정 기간 출근 기록
    public List<WorkRecordResponse> findByPeriod(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        // 방어 로직 (실무 감각 포인트)
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이후일 수 없습니다.");
        }

        List<WorkRecord> records =
                workRecordRepository
                        .findByEmployeeIdAndWorkDateBetween(
                                employeeId,
                                startDate,
                                endDate
                        );

        return workRecordMapper.toResponseList(records);
    }
}