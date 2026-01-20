package com.example.PartTimeHR.schedule.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.exception.EmployeeNotFoundException;
import com.example.PartTimeHR.employee.service.EmployeeAccessService;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.dto.ScheduleCreateRequest;
import com.example.PartTimeHR.schedule.dto.ScheduleResponse;
import com.example.PartTimeHR.schedule.exception.DuplicateScheduleException;
import com.example.PartTimeHR.schedule.mapper.ScheduleMapper;
import com.example.PartTimeHR.schedule.repository.ScheduleRepository;
import com.example.PartTimeHR.schedule.util.ScheduleDateCalculator;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.exception.StoreAccessDeniedException;
import com.example.PartTimeHR.store.exception.StoreNotFoundException;
import com.example.PartTimeHR.store.service.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final StoreAccessService storeAccessService;
    private final EmployeeAccessService employeeAccessService;
    private final ScheduleAccessService scheduleAccessService;
    private final ScheduleMapper scheduleMapper;

    // 스케줄 생성
    @Transactional
    public void createSchedule(
            Long storeId,
            Long employerId,
            ScheduleCreateRequest request
    ) {
        // 매장 접근 권한 검증
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 직원 접근 권한 검증
        Employee employee = employeeAccessService
                .getEmployeeOrThrow(request.getEmployeeId());

        // 시간 검증
        scheduleAccessService.validateWorkTime(
                request.getStartTime(),
                request.getEndTime()
        );

        // 중복 스케줄 방지
        if (scheduleRepository.existsByEmployeeIdAndWorkDate(
                employee.getId(),
                request.getWorkDate()
        )) {
            throw new DuplicateScheduleException();
        }

        Schedule schedule = Schedule.builder()
                .store(store)
                .employee(employee)
                .workDate(request.getWorkDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        scheduleRepository.save(schedule);
    }

    // ===== 전체 직원 조회(단일/기간/주간/월간) =====
    // 직원 전체 단일 날짜 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(
            Long storeId,
            Long employerId,
            LocalDate workDate
    ) {

        Store store = storeAccessService.getMyStore(storeId, employerId);

        return scheduleRepository
                .findByStoreAndWorkDate(storeId, workDate)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }

    // 직원 전체 기간 조회
    public List<ScheduleResponse> findStoreSchedulesByPeriod(
            Long employerId, Long storeId, LocalDate startDate, LocalDate endDate
    ) {
        // StoreAccessService로 검증 + Store 객체 가져오기
        Store store = storeAccessService.getMyStore(storeId, employerId);


        return scheduleRepository.findByStoreAndWorkDateBetween(store, startDate, endDate)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }

    // 직원 전체 주간 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getWeekSchedules(
            Long storeId,
            Long employerId,
            int offset
    ) {
        Store store = storeAccessService.getMyStore(storeId, employerId);

        LocalDate baseDate = LocalDate.now().plusWeeks(offset);

        LocalDate weekStartDate =
                ScheduleDateCalculator.getWeekStartDate(
                        baseDate,
                        store.getWeekStartDay()
                );

        LocalDate weekEndDate = weekStartDate.plusDays(6);

        return scheduleRepository
                .findByStoreAndWorkDateBetween(
                        store,
                        weekStartDate,
                        weekEndDate
                )
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }

    // 직원 전체 월간 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMonthSchedules(
            Long storeId,
            Long employerId,
            int offset
    ) {
        Store store = storeAccessService.getMyStore(storeId, employerId);

        LocalDate baseMonth = LocalDate.now().plusMonths(offset);

        LocalDate monthStart = baseMonth.withDayOfMonth(1);
        LocalDate monthEnd = baseMonth.withDayOfMonth(baseMonth.lengthOfMonth());

        return scheduleRepository
                .findByStoreAndWorkDateBetween(
                        store,
                        monthStart,
                        monthEnd
                )
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }


    // ===== 직원별 조회(단일/기간/주간/월간) =====
    // 직원별 단일 날짜 조회
    public List<ScheduleResponse> findEmployeeSchedulesByDate(
            Long employerId, Long storeId, Long employeeId, LocalDate date
    ) {
        // 1. 매장 접근 권한 체크
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 2. Employee 로딩
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        // 4. 단일 날짜 스케줄 조회


        return scheduleRepository.findByEmployeeAndWorkDate(employee, date)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }

    // 직원별 기간 조회
    public List<ScheduleResponse> findEmployeeSchedulesByPeriod(
            Long employerId, Long employeeId, Long storeId, LocalDate startDate, LocalDate endDate
    ) {
        // 소속 매장 접근 권한 체크
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // Repository에서 Employee 객체 가져오기
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        employeeAccessService.getEmployee(employee.getId(), store);


        return scheduleRepository.findByEmployeeAndWorkDateBetween(employee, startDate, endDate)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }


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
