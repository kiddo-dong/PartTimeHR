package com.example.PartTimeHR.schedule.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.service.EmployeeAccessService;
import com.example.PartTimeHR.schedule.dto.ScheduleResponse;
import com.example.PartTimeHR.schedule.mapper.ScheduleMapper;
import com.example.PartTimeHR.schedule.repository.ScheduleRepository;
import com.example.PartTimeHR.schedule.util.ScheduleDateCalculator;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.service.StoreAccessService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class EmployeeScheduleService {

    public final ScheduleAccessService scheduleAccessService;
    public final ScheduleRepository scheduleRepository;
    public final ScheduleMapper scheduleMapper;
    public final StoreAccessService storeAccessService;
    public final EmployeeAccessService employeeAccessService;

    // 직원별 주간 스케줄 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getEmployeeWeekSchedules(
            Long storeId,
            Long employerId,
            Long employeeId,
            int offset
    ) {
        Store store = storeAccessService.getMyStore(storeId, employerId);
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

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


    // 직원별 월간 스케줄 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getEmployeeMonthSchedules(
            Long storeId,
            Long employerId,
            Long employeeId,
            int offset
    ) {
        Store store = storeAccessService.getMyStore(storeId, employerId);
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

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
