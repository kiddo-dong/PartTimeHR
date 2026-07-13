package com.example.PartTimeHR.payroll.application;

import com.example.PartTimeHR.employee.application.EmployeeAccessService;
import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.payroll.domain.PayrollCalculator;
import com.example.PartTimeHR.payroll.presentation.dto.EmployeePayrollDetailResponse;
import com.example.PartTimeHR.payroll.presentation.dto.EmployeePayrollResponse;
import com.example.PartTimeHR.payroll.presentation.dto.PayrollRecordResponse;
import com.example.PartTimeHR.payroll.presentation.dto.PayrollSummaryResponse;
import com.example.PartTimeHR.payroll.presentation.dto.SeverancePayResponse;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayrollService {

    private final StoreAccessService storeAccessService;
    private final EmployeeAccessService employeeAccessService;
    private final WorkRecordRepository workRecordRepository;
    private final ScheduleRepository scheduleRepository;

    // 매장 전체 급여 요약 (사장)
    public PayrollSummaryResponse getStorePayroll(Long employerId, Long storeId, LocalDate from, LocalDate to) {
        validateRange(from, to);

        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 퇴근 완료된 기록만 급여에 포함
        List<WorkRecord> records = completedOnly(
                workRecordRepository.findAllByStoreAndWorkDateBetween(storeId, from, to)
        );

        Map<Long, Employee> employeesById = new HashMap<>();

        Map<Long, List<WorkRecord>> recordsByEmployee = new HashMap<>();
        for (WorkRecord record : records) {
            recordsByEmployee
                    .computeIfAbsent(record.getEmployee().getId(), key -> new ArrayList<>())
                    .add(record);
            employeesById.putIfAbsent(record.getEmployee().getId(), record.getEmployee());
        }

        // 개근 판정·유급휴일수당용 스케줄 (직원별)
        // 기록이 없어도 휴일 스케줄이 있으면 유급휴일수당 대상이므로 직원 목록에 포함
        Map<Long, List<Schedule>> schedulesByEmployee = new HashMap<>();
        for (Schedule schedule : scheduleRepository.findByStoreAndWorkDateBetween(store, from, to)) {
            schedulesByEmployee
                    .computeIfAbsent(schedule.getEmployee().getId(), key -> new ArrayList<>())
                    .add(schedule);
            employeesById.putIfAbsent(schedule.getEmployee().getId(), schedule.getEmployee());
        }

        List<EmployeePayrollResponse> employees = new ArrayList<>();
        long totalPay = 0;

        for (Employee employee : employeesById.values()) {
            List<WorkRecord> employeeRecords = recordsByEmployee.getOrDefault(employee.getId(), List.of());

            PayrollCalculator.Result result = PayrollCalculator.calculate(
                    employeeRecords,
                    schedulesByEmployee.getOrDefault(employee.getId(), List.of()),
                    List.of(), // 승인된 연차 사용일 (연차 도메인 연동 예정)
                    calcParams(store, employee)
            );

            // 급여가 전혀 발생하지 않은 직원은 요약에서 제외
            if (result.totalPay() == 0 && employeeRecords.isEmpty()) {
                continue;
            }

            totalPay += result.totalPay();

            employees.add(EmployeePayrollResponse.builder()
                    .employeeId(employee.getId())
                    .employeeName(employee.getName())
                    .recordCount(employeeRecords.size())
                    .totalNetMinutes(result.totalNetMinutes())
                    .basePay(result.basePay())
                    .weeklyAllowance(result.weeklyAllowance())
                    .overtimeAllowance(result.overtimeAllowance())
                    .nightAllowance(result.nightAllowance())
                    .holidayAllowance(result.holidayAllowance())
                    .holidayLeavePay(result.holidayLeavePay())
                    .totalPay(result.totalPay())
                    .build());
        }

        employees.sort(Comparator.comparing(EmployeePayrollResponse::getEmployeeName,
                Comparator.nullsLast(String::compareTo)));

        return PayrollSummaryResponse.builder()
                .storeId(storeId)
                .from(from)
                .to(to)
                .weeklyAllowanceIncluded(Boolean.TRUE.equals(store.getWeeklyAllowanceIncluded()))
                .totalPay(totalPay)
                .employees(employees)
                .build();
    }

    // 직원별 급여 상세 (사장)
    public EmployeePayrollDetailResponse getEmployeePayroll(
            Long employerId, Long storeId, Long employeeId, LocalDate from, LocalDate to
    ) {
        validateRange(from, to);

        Store store = storeAccessService.getMyStore(storeId, employerId);
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

        return buildDetail(employee, store, from, to);
    }

    // 본인 급여 상세 (직원)
    public EmployeePayrollDetailResponse getMyPayroll(Long employeeId, LocalDate from, LocalDate to) {
        validateRange(from, to);

        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        return buildDetail(employee, employee.getStore(), from, to);
    }

    private EmployeePayrollDetailResponse buildDetail(Employee employee, Store store, LocalDate from, LocalDate to) {
        List<WorkRecord> records = completedOnly(
                workRecordRepository.findAllByEmployeeAndWorkDateBetween(employee, from, to)
        );

        List<Schedule> schedules = scheduleRepository
                .findByEmployeeAndWorkDateBetween(employee, from, to);

        PayrollCalculator.Result result = PayrollCalculator.calculate(
                records,
                schedules,
                List.of(), // 승인된 연차 사용일 (연차 도메인 연동 예정)
                calcParams(store, employee)
        );

        List<PayrollRecordResponse> recordResponses = records.stream()
                .sorted(Comparator.comparing(WorkRecord::getWorkDate))
                .map(record -> PayrollRecordResponse.builder()
                        .workRecordId(record.getId())
                        .workDate(record.getWorkDate())
                        .netWorkedMinutes(record.getActualWorkMinutes().intValue())
                        .appliedHourlyWage(record.getAppliedHourlyWage())
                        .appliedJobTitle(record.getAppliedJobTitle())
                        .pay(PayrollCalculator.recordPay(record))
                        .build())
                .toList();

        return EmployeePayrollDetailResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .from(from)
                .to(to)
                .weeklyAllowanceIncluded(Boolean.TRUE.equals(store.getWeeklyAllowanceIncluded()))
                .recordCount(records.size())
                .totalNetMinutes(result.totalNetMinutes())
                .basePay(result.basePay())
                .weeklyAllowance(result.weeklyAllowance())
                .overtimeAllowance(result.overtimeAllowance())
                .nightAllowance(result.nightAllowance())
                .holidayAllowance(result.holidayAllowance())
                .holidayLeavePay(result.holidayLeavePay())
                .totalPay(result.totalPay())
                .records(recordResponses)
                .build();
    }

    // 퇴직금 추정 (사장)
    public SeverancePayResponse getSeverancePay(Long employerId, Long storeId, Long employeeId, LocalDate asOf) {
        Store store = storeAccessService.getMyStore(storeId, employerId);
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

        return buildSeverancePay(employee, store, asOf != null ? asOf : LocalDate.now());
    }

    // 퇴직금 추정 (본인)
    public SeverancePayResponse getMySeverancePay(Long employeeId, LocalDate asOf) {
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        return buildSeverancePay(employee, employee.getStore(), asOf != null ? asOf : LocalDate.now());
    }

    /**
     * 퇴직금 추정 = 1일 평균임금 × 30 × (재직일수 / 365)
     * - 평균임금: 기준일 이전 3개월 임금총액(수당 포함) ÷ 총 달력일수
     * - 알바처럼 근무일이 적으면 평균임금이 낮게 나오므로,
     *   통상임금 추정치(시급 × 1일 평균 실근무시간)보다 낮으면 그것으로 보정 (제2조 2항)
     * - 자격: 재직 1년 이상 + 최근 4주 평균 주 15시간 이상
     */
    private SeverancePayResponse buildSeverancePay(Employee employee, Store store, LocalDate asOf) {
        SeverancePayResponse.SeverancePayResponseBuilder builder = SeverancePayResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .hiredAt(employee.getHiredAt())
                .asOf(asOf);

        if (employee.getHiredAt() == null) {
            return builder.eligible(false).ineligibleReason("입사일이 등록되지 않았습니다.").build();
        }

        long serviceDays = ChronoUnit.DAYS.between(employee.getHiredAt(), asOf);
        builder.serviceDays(serviceDays);

        if (serviceDays < 365) {
            return builder.eligible(false).ineligibleReason("재직 기간이 1년 미만입니다.").build();
        }

        // 최근 4주 평균 주 15시간 검사
        List<WorkRecord> lastFourWeeks = completedOnly(
                workRecordRepository.findAllByEmployeeAndWorkDateBetween(employee, asOf.minusDays(28), asOf.minusDays(1))
        );
        long fourWeekMinutes = lastFourWeeks.stream()
                .mapToLong(WorkRecord::getActualWorkMinutes)
                .sum();
        if (fourWeekMinutes / 4 < 15 * 60) {
            return builder.eligible(false)
                    .ineligibleReason("최근 4주 평균 주 소정근로시간이 15시간 미만입니다.").build();
        }

        // 평균임금: 기준일 이전 3개월
        LocalDate from = asOf.minusMonths(3);
        LocalDate to = asOf.minusDays(1);
        long periodDays = ChronoUnit.DAYS.between(from, asOf);

        List<WorkRecord> records = completedOnly(
                workRecordRepository.findAllByEmployeeAndWorkDateBetween(employee, from, to)
        );
        List<Schedule> schedules = scheduleRepository.findByEmployeeAndWorkDateBetween(employee, from, to);

        PayrollCalculator.Result pay = PayrollCalculator.calculate(
                records, schedules, List.of(), calcParams(store, employee)
        );

        long averageDailyWage = Math.round((double) pay.totalPay() / periodDays);

        // 통상임금 보정: 시급 × 1일 평균 실근무시간(8시간 한도)
        if (!records.isEmpty()) {
            double avgDailyHours = Math.min(
                    pay.totalNetMinutes() / 60.0 / records.size(), 8.0
            );
            long ordinaryDailyWage = Math.round(
                    avgDailyHours * employee.getPayPolicy().getHourlyWage()
            );
            averageDailyWage = Math.max(averageDailyWage, ordinaryDailyWage);
        }

        long estimated = Math.round(averageDailyWage * 30.0 * serviceDays / 365.0);

        return builder
                .eligible(true)
                .averageDailyWage(averageDailyWage)
                .estimatedAmount(estimated)
                .build();
    }

    private PayrollCalculator.Params calcParams(Store store, Employee employee) {
        return new PayrollCalculator.Params(
                store.getWeekStartDay(),
                store.getWeeklyAllowanceIncluded(),
                store.getFiveOrMoreEmployees(),
                employee.getPayPolicy().getHourlyWage(),
                employee.getWeeklyRestDay()
        );
    }

    private List<WorkRecord> completedOnly(List<WorkRecord> records) {
        return records.stream()
                .filter(record -> record.getClockOutTime() != null)
                .toList();
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("조회 시작일/종료일은 필수입니다.");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("조회 종료일은 시작일보다 빠를 수 없습니다.");
        }
    }
}
