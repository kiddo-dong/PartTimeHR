package com.example.PartTimeHR.workrecord.repository;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeWorkRecordRepository extends JpaRepository<WorkRecord, Long> {

    // ====== 출퇴근/휴게 기록용 (Action API 및 서비스) ======
    // 특정 직원의 날짜 범위 기록 조회
    List<WorkRecord> findByEmployeeAndWorkDateBetween(
            Employee employee,
            LocalDate startDate,
            LocalDate endDate);


    // ====== Employee Query(조회용) ======
    // employeeId로 특정 기간 조회
    List<WorkRecord> findByEmployeeIdAndWorkDateBetween(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 특정 직원의 특정 날짜 기록 조회
    List<WorkRecord> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);
}

