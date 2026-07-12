package com.example.PartTimeHR.workrecord.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkRecordRepository extends JpaRepository<WorkRecord, Long> {

    // 직원의 특정 날짜 모든 근무 기록 조회
    List<WorkRecord> findAllByEmployeeAndWorkDate(Employee employee, LocalDate workDate);

    // 직원의 특정 기간 근무 기록 조회
    List<WorkRecord> findAllByEmployeeAndWorkDateBetween(
            Employee employee,
            LocalDate startDate,
            LocalDate endDate
    );

    // 현재 진행 중인 근무 조회
    Optional<WorkRecord> findFirstByEmployeeAndClockOutTimeIsNullOrderByClockInTimeDesc(
            Employee employee
    );

    // 퇴근하지 않은 근무 존재 여부 확인
    boolean existsByEmployeeAndClockOutTimeIsNull(Employee employee);

    // 직원의 전체 근무 기록 조회
    List<WorkRecord> findAllByEmployee(Employee employee);

    // 집계 시 employee를 함께 읽으므로 fetch join으로 N+1 방지
    @Query("""
            select wr
            from WorkRecord wr
            join fetch wr.employee
            where wr.employee.store.id = :storeId
              and wr.workDate = :workDate
            """)
    List<WorkRecord> findAllByStoreAndWorkDate(Long storeId, LocalDate workDate);

    @Query("""
            select wr
            from WorkRecord wr
            join fetch wr.employee
            where wr.employee.store.id = :storeId
              and wr.workDate between :startDate and :endDate
            """)
    List<WorkRecord> findAllByStoreAndWorkDateBetween(Long storeId, LocalDate startDate, LocalDate endDate);
}
