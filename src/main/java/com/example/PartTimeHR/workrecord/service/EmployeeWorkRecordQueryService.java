package com.example.PartTimeHR.workrecord.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.exception.EmployeeNotFoundException;
import com.example.PartTimeHR.workrecord.exception.WorkRecordNotFoundException;
import com.example.PartTimeHR.workrecord.mapper.WorkRecordMapper;
import com.example.PartTimeHR.workrecord.repository.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class EmployeeWorkRecordQueryService {

    private final WorkRecordRepository workRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkRecordMapper workRecordMapper;

    // 오늘 근무 기록 조회
    public WorkRecordResponse today(String email) {

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(EmployeeNotFoundException::new);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        WorkRecord workRecord = workRecordRepository
                .findByEmployeeAndWorkDate(employee, today)
                .orElseThrow(() -> new WorkRecordNotFoundException("오늘의 근무 기록이 없습니다."));

        return workRecordMapper.toResponse(workRecord);
    }

}