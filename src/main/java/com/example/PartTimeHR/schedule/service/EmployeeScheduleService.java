package com.example.PartTimeHR.schedule.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employee.service.EmployeeAccessService;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.dto.ScheduleResponse;
import com.example.PartTimeHR.schedule.mapper.ScheduleMapper;
import com.example.PartTimeHR.schedule.repository.ScheduleRepository;
import com.example.PartTimeHR.schedule.util.ScheduleDateCalculator;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.service.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeScheduleService {

    public final ScheduleRepository scheduleRepository;
    public final ScheduleMapper scheduleMapper;
    public final EmployeeAccessService employeeAccessService;

    // 직원의 일별 스케줄 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getTodaySchedule(Long employeeId, int offset){
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        Store store = employee.getStore();

        LocalDate today = LocalDate.now();

        return scheduleRepository
                .findByEmployeeAndWorkDate(employee, today)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }

    // 기간 조회
    public List<ScheduleResponse> getSchedulesByPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        List<Schedule> schedules = scheduleRepository.findByEmployeeAndWorkDateBetween(employee, startDate, endDate);

        return schedules.stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }

    // 직원의 주간 스케줄 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getWeekSchedules(Long employeeId, int offset) {

        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        Store store = employee.getStore();

        LocalDate baseDate = LocalDate.now().plusWeeks(offset);

        LocalDate weekStart =
                ScheduleDateCalculator.getWeekStartDate(
                        baseDate,
                        store.getWeekStartDay()
                );

        LocalDate weekEnd = weekStart.plusDays(6);

        return scheduleRepository
                .findByEmployeeAndWorkDateBetween(
                        employee,
                        weekStart,
                        weekEnd
                )
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }

    // 직원의 월별 스케줄 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMonthSchedules(Long employeeId, int offset) {
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);
        Store store = employee.getStore();

        LocalDate baseMonth = LocalDate.now().plusMonths(offset);

        LocalDate monthStart = baseMonth.withDayOfMonth(1);
        LocalDate monthEnd = baseMonth.withDayOfMonth(baseMonth.lengthOfMonth());

        return scheduleRepository
                .findByEmployeeAndWorkDateBetween(
                        employee,
                        monthStart,
                        monthEnd
                )
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }
}
