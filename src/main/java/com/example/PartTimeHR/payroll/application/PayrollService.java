package com.example.PartTimeHR.payroll.application;

import com.example.PartTimeHR.employee.application.EmployeeAccessService;
import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.payroll.domain.PayrollCalculator;
import com.example.PartTimeHR.payroll.presentation.dto.EmployeePayrollDetailResponse;
import com.example.PartTimeHR.payroll.presentation.dto.EmployeePayrollResponse;
import com.example.PartTimeHR.payroll.presentation.dto.PayrollRecordResponse;
import com.example.PartTimeHR.payroll.presentation.dto.PayrollSummaryResponse;
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
                    store.getWeekStartDay(),
                    store.getWeeklyAllowanceIncluded(),
                    store.getFiveOrMoreEmployees(),
                    employee.getPayPolicy().getHourlyWage()
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
                store.getWeekStartDay(),
                store.getWeeklyAllowanceIncluded(),
                store.getFiveOrMoreEmployees(),
                employee.getPayPolicy().getHourlyWage()
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
