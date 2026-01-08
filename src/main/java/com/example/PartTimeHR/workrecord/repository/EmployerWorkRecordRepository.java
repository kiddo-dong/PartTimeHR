package com.example.PartTimeHR.workrecord.repository;

import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployerWorkRecordRepository extends JpaRepository<WorkRecord, Long>{
    // 오늘 전체 기록 조회
    @Query("SELECT w FROM WorkRecord w WHERE w.workDate = :today")
    List<WorkRecord> findByToday(@Param("today") LocalDate today);

    // 특정 직원 + 기간
    List<WorkRecord> findByEmployeeIdAndWorkDateBetween(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 사장님 가게 전체 직원 + 기간
    @Query("""
        select wr
        from WorkRecord wr
        join wr.employee e
        where e.employer.id = :employerId
          and wr.workDate between :startDate and :endDate
    """)
    List<WorkRecord> findByEmployerAndPeriod(
            @Param("employerId") Long employerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}