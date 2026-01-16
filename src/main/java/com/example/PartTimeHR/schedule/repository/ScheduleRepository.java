package com.example.PartTimeHR.schedule.repository;

import com.example.PartTimeHR.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByEmployeeId(Long employeeId);

    List<Schedule> findByStoreId(Long storeId);

}
