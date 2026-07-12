package com.example.PartTimeHR.workrecord.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkRecordRepository extends JpaRepository<WorkRecord, Long> {

    // 집계가 breaks에서 파생되므로 조회 시 함께 로딩 (N+1 방지)

    @EntityGraph(attributePaths = "breaks")
    List<WorkRecord> findAllByEmployeeAndWorkDate(Employee employee, LocalDate workDate);

    @EntityGraph(attributePaths = "breaks")
    List<WorkRecord> findAllByEmployeeAndWorkDateBetween(
            Employee employee,
            LocalDate startDate,
            LocalDate endDate
    );

    // 현재 진행 중인 근무 조회
    @EntityGraph(attributePaths = "breaks")
    Optional<WorkRecord> findFirstByEmployeeAndClockOutTimeIsNullOrderByClockInTimeDesc(
            Employee employee
    );

    // 퇴근하지 않은 근무 존재 여부 확인
    boolean existsByEmployeeAndClockOutTimeIsNull(Employee employee);

    @Query("""
            select wr
            from WorkRecord wr
            join fetch wr.employee
            left join fetch wr.breaks
            where wr.employee.store.id = :storeId
              and wr.workDate = :workDate
            """)
    List<WorkRecord> findAllByStoreAndWorkDate(Long storeId, LocalDate workDate);

    @Query("""
            select wr
            from WorkRecord wr
            join fetch wr.employee
            left join fetch wr.breaks
            where wr.employee.store.id = :storeId
              and wr.workDate between :startDate and :endDate
            """)
    List<WorkRecord> findAllByStoreAndWorkDateBetween(Long storeId, LocalDate startDate, LocalDate endDate);

    // 직원 삭제 시 함께 정리
    void deleteAllByEmployee(Employee employee);
}
