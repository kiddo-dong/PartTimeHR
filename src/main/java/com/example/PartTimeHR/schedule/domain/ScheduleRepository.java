package com.example.PartTimeHR.schedule.domain;

import  com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 매장 + 날짜별 조회
    List<Schedule> findByStoreAndWorkDate(Store store, LocalDate workDate);

    // 직원 중복 스케줄 방지
    boolean existsByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    @Query("""
    select count(s) > 0
    from Schedule s
    where s.employee.id = :employeeId
      and s.workDate = :workDate
      and s.startTime < :endTime
      and s.endTime > :startTime
    """)
    boolean existsOverlappingSchedule(
            Long employeeId,
            LocalDate workDate,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<Schedule> findByStoreAndWorkDateBetween(
            Store store,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Schedule> findByEmployeeAndWorkDateBetween(
            Employee employee,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Schedule> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);

    List<Schedule> findByEmployeeAndStoreAndWorkDate(Employee employee, Store store, LocalDate workDate);

}