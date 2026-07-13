package com.example.PartTimeHR.leave.application;

import com.example.PartTimeHR.employee.application.EmployeeAccessService;
import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.leave.domain.LeaveRequest;
import com.example.PartTimeHR.leave.domain.LeaveRequestRepository;
import com.example.PartTimeHR.leave.domain.LeaveStatus;
import com.example.PartTimeHR.leave.presentation.dto.LeaveBalanceResponse;
import com.example.PartTimeHR.leave.presentation.dto.LeaveResponse;
import com.example.PartTimeHR.payroll.domain.KoreanHolidayCalendar;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.domain.ScheduleRepository;
import com.example.PartTimeHR.store.application.StoreAccessService;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 연차유급휴가 (근로기준법 제60조, 상시 5인 이상 사업장).
 * - 1년 미만: 개근한 달마다 1일 (최대 11일)
 * - 1년 이상: 연 15일 + 3년차부터 2년마다 1일 가산 (최대 25일)
 * 단순화: 이월 없음, 1년 이상의 80% 출근율 요건은 판정하지 않음,
 *         스케줄이 없는 달은 개근으로 간주 (스케줄 미사용 매장 대응)
 */
@Service
@RequiredArgsConstructor
public class LeaveService {

    private static final int FIRST_YEAR_MAX_DAYS = 11;
    private static final int BASE_ANNUAL_DAYS = 15;
    private static final int MAX_ANNUAL_DAYS = 25;

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeAccessService employeeAccessService;
    private final StoreAccessService storeAccessService;
    private final ScheduleRepository scheduleRepository;
    private final WorkRecordRepository workRecordRepository;

    /* ===== 직원 ===== */

    // 연차 신청
    @Transactional
    public LeaveResponse request(Long employeeId, LocalDate leaveDate) {
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        requireApplicable(employee.getStore());

        if (leaveDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("지난 날짜에는 연차를 신청할 수 없습니다.");
        }

        if (leaveRequestRepository.existsByEmployeeAndLeaveDateAndStatusIn(
                employee, leaveDate, List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED))) {
            throw new IllegalStateException("해당 날짜에 이미 신청되었거나 승인된 연차가 있습니다.");
        }

        LeaveBalanceResponse balance = buildBalance(employee);
        long pendingCount = leaveRequestRepository.countByEmployeeAndStatus(employee, LeaveStatus.PENDING);
        if (balance.getRemainingDays() - pendingCount < 1) {
            throw new IllegalStateException("잔여 연차가 없습니다.");
        }

        LeaveRequest leave = leaveRequestRepository.save(LeaveRequest.create(employee, leaveDate));
        return LeaveResponse.from(leave);
    }

    // 본인 신청 내역
    @Transactional(readOnly = true)
    public List<LeaveResponse> getMyLeaves(Long employeeId) {
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        return leaveRequestRepository.findAllByEmployeeOrderByLeaveDateDesc(employee)
                .stream()
                .map(LeaveResponse::from)
                .toList();
    }

    // 본인 잔여 연차
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getMyBalance(Long employeeId) {
        return buildBalance(employeeAccessService.getEmployeeOrThrow(employeeId));
    }

    /* ===== 사장 ===== */

    // 매장 신청 목록 (status 생략 시 전체)
    @Transactional(readOnly = true)
    public List<LeaveResponse> getStoreLeaves(Long employerId, Long storeId, LeaveStatus status) {
        storeAccessService.getMyStore(storeId, employerId);

        List<LeaveRequest> leaves = status == null
                ? leaveRequestRepository.findAllByEmployee_Store_IdOrderByLeaveDateDesc(storeId)
                : leaveRequestRepository.findAllByEmployee_Store_IdAndStatusOrderByLeaveDateDesc(storeId, status);

        return leaves.stream().map(LeaveResponse::from).toList();
    }

    // 승인/거절
    @Transactional
    public LeaveResponse decide(Long employerId, Long storeId, Long leaveId, boolean approve) {
        storeAccessService.getMyStore(storeId, employerId);

        LeaveRequest leave = leaveRequestRepository.findByIdAndEmployee_Store_Id(leaveId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 연차 신청을 찾을 수 없습니다."));

        if (approve) {
            leave.approve();
        } else {
            leave.reject();
        }

        return LeaveResponse.from(leave);
    }

    // 직원 잔여 연차 (사장)
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getBalance(Long employerId, Long storeId, Long employeeId) {
        Store store = storeAccessService.getMyStore(storeId, employerId);
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

        return buildBalance(employee);
    }

    /* ===== 내부 ===== */

    private void requireApplicable(Store store) {
        if (!Boolean.TRUE.equals(store.getFiveOrMoreEmployees())) {
            throw new IllegalStateException("법정 연차는 상시 5인 이상 사업장에 적용됩니다.");
        }
    }

    private LeaveBalanceResponse buildBalance(Employee employee) {
        LeaveBalanceResponse.LeaveBalanceResponseBuilder builder = LeaveBalanceResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .hiredAt(employee.getHiredAt());

        boolean applicable = Boolean.TRUE.equals(employee.getStore().getFiveOrMoreEmployees())
                && employee.getHiredAt() != null;

        if (!applicable) {
            return builder.applicable(false).grantedDays(0).usedDays(0).remainingDays(0).build();
        }

        LocalDate hiredAt = employee.getHiredAt();
        LocalDate today = LocalDate.now();
        long serviceDays = ChronoUnit.DAYS.between(hiredAt, today);

        int granted;
        int used;

        if (serviceDays < 365) {
            // 1년 미만: 개근한 달마다 1일
            granted = countPerfectMonths(employee, hiredAt, today);
            used = (int) leaveRequestRepository.countByEmployeeAndStatusAndLeaveDateBetween(
                    employee, LeaveStatus.APPROVED, hiredAt, hiredAt.plusYears(1).minusDays(1));
        } else {
            // 1년 이상: 현재 연차년도 기준 15일 + 가산
            int years = (int) (serviceDays / 365);
            granted = Math.min(BASE_ANNUAL_DAYS + Math.max(0, (years - 1) / 2), MAX_ANNUAL_DAYS);

            LocalDate leaveYearStart = hiredAt.plusYears(years);
            used = (int) leaveRequestRepository.countByEmployeeAndStatusAndLeaveDateBetween(
                    employee, LeaveStatus.APPROVED, leaveYearStart, leaveYearStart.plusYears(1).minusDays(1));
        }

        return builder
                .applicable(true)
                .grantedDays(granted)
                .usedDays(used)
                .remainingDays(Math.max(0, granted - used))
                .build();
    }

    /**
     * 입사 후 완료된 '개근 달' 수 (1년 미만 연차 발생용, 최대 11).
     * 개근: 그 달의 스케줄 날짜(유급휴일·승인 연차 제외)마다 근무 기록이 존재.
     * 스케줄이 없는 달은 개근으로 간주 (스케줄 미사용 매장).
     */
    private int countPerfectMonths(Employee employee, LocalDate hiredAt, LocalDate today) {
        List<Schedule> schedules = scheduleRepository.findByEmployeeAndWorkDateBetween(employee, hiredAt, today);
        Set<LocalDate> workedDays = workRecordRepository
                .findAllByEmployeeAndWorkDateBetween(employee, hiredAt, today)
                .stream()
                .map(WorkRecord::getWorkDate)
                .collect(Collectors.toSet());
        Set<LocalDate> approvedLeaves = leaveRequestRepository
                .findAllByEmployeeAndStatusAndLeaveDateBetween(employee, LeaveStatus.APPROVED, hiredAt, today)
                .stream()
                .map(LeaveRequest::getLeaveDate)
                .collect(Collectors.toSet());

        boolean fiveOrMore = Boolean.TRUE.equals(employee.getStore().getFiveOrMoreEmployees());
        Integer restDay = employee.getWeeklyRestDay();

        int perfectMonths = 0;
        for (int month = 0; month < FIRST_YEAR_MAX_DAYS; month++) {
            LocalDate monthStart = hiredAt.plusMonths(month);
            LocalDate monthEnd = hiredAt.plusMonths(month + 1).minusDays(1);

            if (monthEnd.isAfter(today.minusDays(1))) {
                break; // 아직 완료되지 않은 달
            }

            boolean perfect = schedules.stream()
                    .map(Schedule::getWorkDate)
                    .filter(date -> !date.isBefore(monthStart) && !date.isAfter(monthEnd))
                    .filter(date -> !KoreanHolidayCalendar.isPaidHoliday(date, fiveOrMore))
                    .filter(date -> restDay == null || date.getDayOfWeek().getValue() != restDay)
                    .filter(date -> !approvedLeaves.contains(date))
                    .allMatch(workedDays::contains);

            if (perfect) {
                perfectMonths++;
            }
        }
        return perfectMonths;
    }
}
