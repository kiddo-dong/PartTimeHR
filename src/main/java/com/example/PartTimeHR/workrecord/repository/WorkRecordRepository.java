package com.example.PartTimeHR.workrecord.repository;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkRecordRepository extends JpaRepository<WorkRecord, Long> {

    // 특정 직원의 특정 날짜 기록 조회
    Optional<WorkRecord> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);

    // 특정 직원의 모든 기록 조회
    List<WorkRecord> findByEmployee(Employee employee);

    // 특정 직원의 날짜 범위 기록 조회
    List<WorkRecord> findByEmployeeAndWorkDateBetween(
            Employee employee,
            LocalDate startDate,
            LocalDate endDate
    );

    // 중복 출근 체크
    boolean existsByEmployeeAndWorkDate(Employee employee, LocalDate workDate);

    // 특정 직원의 기록 조회 (날짜 범위)
    List<WorkRecord> findByEmployeeOrderByWorkDateDesc(Employee employee);
}

