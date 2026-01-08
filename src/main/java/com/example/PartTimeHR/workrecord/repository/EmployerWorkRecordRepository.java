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
}
