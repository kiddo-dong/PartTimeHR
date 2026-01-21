package com.example.PartTimeHR.workrecord.repository;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkRecordRepository extends JpaRepository<WorkRecord, Long> {

    // 오늘 하루 기록 전체 (출근 중복 체크용)
    List<WorkRecord> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);

    // 오늘의 "가장 최근 + 미완료" 기록 (핵심)
    Optional<WorkRecord>
    findTopByEmployeeAndWorkDateAndStatusNotOrderByClockInTimeDesc(
            Employee employee,
            LocalDate workDate,
            WorkStatus status
    );
}
