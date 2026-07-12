package com.example.PartTimeHR.schedule.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.store.domain.Store;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 매장 + 날짜별 조회 (응답 변환 시 employee.name을 읽으므로 함께 로딩)
    @EntityGraph(attributePaths = "employee")
    List<Schedule> findByStoreAndWorkDate(Store store, LocalDate workDate);

    // 근무 시간 겹침 검사 (같은 날짜에 시간대가 겹치는 스케줄 존재 여부)
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

    @EntityGraph(attributePaths = "employee")
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
