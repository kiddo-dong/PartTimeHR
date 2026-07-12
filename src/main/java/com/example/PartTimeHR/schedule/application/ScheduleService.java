package com.example.PartTimeHR.schedule.application;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.application.EmployeeAccessService;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.presentation.dto.ScheduleCreateRequest;
import com.example.PartTimeHR.schedule.presentation.dto.ScheduleResponse;
import com.example.PartTimeHR.schedule.presentation.dto.ScheduleUpdateRequest;
import com.example.PartTimeHR.schedule.domain.ScheduleRepository;
import com.example.PartTimeHR.schedule.domain.ScheduleDateCalculator;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.application.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 직원 조회 + 해당 매장 소속 검증
        Employee employee = employeeAccessService
                .getEmployee(request.getEmployeeId(), store);

        // 시간 검증
        scheduleAccessService.validateWorkTime(
                request.getStartTime(),
                request.getEndTime()
        );

        // 시간 겹침 검증 (겹치지만 않으면 하루에 여러 타임 근무 가능)
        scheduleAccessService.validateNoOverlap(
                employee.getId(),
                request.getStartTime().toLocalDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        // workDate는 startTime에서 유도된다
        Schedule schedule = Schedule.create(
                store,
                employee,
                request.getStartTime(),
                request.getEndTime()
        );

        scheduleRepository.save(schedule);
    }

    // ===== 수정 =====
    @Transactional
    public void updateSchedule(
            Long employerId,
            Long storeId,
            Long employeeId,
            Long scheduleId,
            ScheduleUpdateRequest request
    ) {
        // 매장 권한
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 직원 조회 + 해당 매장 소속 검증
        employeeAccessService.getEmployee(employeeId, store);

        // 스케줄 조회
        Schedule schedule = scheduleAccessService.getScheduleOrThrow(scheduleId);

        // 스케줄-직원-매장 일치 검증
        if (!schedule.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("스케줄과 직원 정보가 일치하지 않습니다.");
        }

        // 시간 검증 (야간 포함 OK)
        scheduleAccessService.validateWorkTime(
                request.getStartTime(),
                request.getEndTime()
        );

        // 수정 결과가 다른 스케줄과 겹치지 않는지 검증 (자기 자신은 제외)
        // 날짜는 새 startTime 기준 - updateTime이 workDate도 함께 갱신한다
        scheduleAccessService.validateNoOverlap(
                employeeId,
                request.getStartTime().toLocalDate(),
                request.getStartTime(),
                request.getEndTime(),
                schedule.getId()
        );

        // 수정
        schedule.updateTime(
                request.getStartTime(),
                request.getEndTime()
        );
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
                .findByStoreAndWorkDate(store, workDate)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();

    }


    // 직원 전체 기간 조회
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public List<ScheduleResponse> findEmployeeSchedulesByDate(
            Long employerId, Long storeId, Long employeeId, LocalDate date
    ) {
        // 매장 접근 권한 체크
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 직원 조회 + 해당 매장 소속 검증
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

        // 단일 날짜 스케줄 조회
        return scheduleRepository
                .findByEmployeeAndStoreAndWorkDate(employee, store, date)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();

    }

    // 직원별 기간 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> findEmployeeSchedulesByPeriod(
            Long employerId, Long employeeId, Long storeId, LocalDate startDate, LocalDate endDate
    ) {
        // 소속 매장 접근 권한 체크
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 직원 조회 + 해당 매장 소속 검증
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

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

    // ===== 삭제 =====
    @Transactional
    public void deleteSchedule(
            Long employerId,
            Long storeId,
            Long employeeId,
            Long scheduleId
    ) {
        // 매장 접근 권한 검증
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 직원 조회 + 해당 매장 소속 검증
        employeeAccessService.getEmployee(employeeId, store);

        // 스케줄 조회
        Schedule schedule = scheduleAccessService.getScheduleOrThrow(scheduleId);

        // 스케줄-직원 일치 검증
        if (!schedule.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("해당 직원의 스케줄이 아닙니다.");
        }

        // 삭제
        scheduleRepository.delete(schedule);
    }
}