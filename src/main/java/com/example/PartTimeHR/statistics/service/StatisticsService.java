package com.example.PartTimeHR.statistics.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.statistics.dto.*;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.domain.WorkStatus;
import com.example.PartTimeHR.workrecord.mapper.WorkRecordMapper;
import com.example.PartTimeHR.workrecord.repository.WorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final WorkRecordRepository workRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployerRepository employerRepository;
    private final WorkRecordMapper workRecordMapper;

    // 대시보드 통계
    @Transactional(readOnly = true)
    public DashboardStatisticsResponse getDashboardStatistics(String employerEmail) {
        Employer employer = employerRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        // 전체 직원 수
        List<Employee> employees = employeeRepository.findByEmployer(employer);
        Long totalEmployeeCount = (long) employees.size();

        // 오늘 출근 기록
        List<WorkRecord> todayRecords = employees.stream()
                .flatMap(emp -> workRecordRepository.findByEmployeeAndWorkDateBetween(emp, today, today).stream())
                .toList();

        // 오늘 통계
        Long todayClockInCount = (long) todayRecords.size();
        Long todayNotClockOutCount = todayRecords.stream()
                .filter(record -> record.getStatus() != WorkStatus.COMPLETED)
                .count();

        Double todayTotalWorkHours = todayRecords.stream()
                .map(workRecordMapper::calculateTotalWorkHours)
                .filter(hours -> hours != null)
                .reduce(0.0, Double::sum);

        Double todayTotalActualWorkHours = todayRecords.stream()
                .map(workRecordMapper::calculateActualWorkHours)
                .filter(hours -> hours != null)
                .reduce(0.0, Double::sum);

        // 오늘 출근한 직원 목록
        List<TodayEmployeeInfo> todayEmployees = todayRecords.stream()
                .map(record -> TodayEmployeeInfo.builder()
                        .employeeId(record.getEmployee().getId())
                        .employeeName(record.getEmployee().getName())
                        .clockInTime(record.getClockInTime())
                        .clockOutTime(record.getClockOutTime())
                        .status(record.getStatus().name())
                        .actualWorkHours(workRecordMapper.calculateActualWorkHours(record))
                        .build())
                .collect(Collectors.toList());

        // 이번 달 기록
        List<WorkRecord> thisMonthRecords = employees.stream()
                .flatMap(emp -> workRecordRepository.findByEmployeeAndWorkDateBetween(emp, firstDayOfMonth, today).stream())
                .toList();

        // 이번 달 통계
        Double thisMonthTotalWorkHours = thisMonthRecords.stream()
                .map(workRecordMapper::calculateTotalWorkHours)
                .filter(hours -> hours != null)
                .reduce(0.0, Double::sum);

        Double thisMonthTotalActualWorkHours = thisMonthRecords.stream()
                .map(workRecordMapper::calculateActualWorkHours)
                .filter(hours -> hours != null)
                .reduce(0.0, Double::sum);

        Long thisMonthWorkDays = thisMonthRecords.stream()
                .map(WorkRecord::getWorkDate)
                .distinct()
                .count();

        return DashboardStatisticsResponse.builder()
                .todayClockInCount(todayClockInCount)
                .todayNotClockOutCount(todayNotClockOutCount)
                .todayTotalWorkHours(todayTotalWorkHours)
                .todayTotalActualWorkHours(todayTotalActualWorkHours)
                .thisMonthTotalWorkHours(thisMonthTotalWorkHours)
                .thisMonthTotalActualWorkHours(thisMonthTotalActualWorkHours)
                .thisMonthWorkDays(thisMonthWorkDays)
                .totalEmployeeCount(totalEmployeeCount)
                .todayEmployees(todayEmployees)
                .build();
    }

    // 직원별 통계
    @Transactional(readOnly = true)
    public EmployeeStatisticsResponse getEmployeeStatistics(String employerEmail, Long employeeId, LocalDate startDate, LocalDate endDate) {
        Employer employer = employerRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));

        // 자신의 직원인지 확인
        if (!employee.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원의 통계만 조회할 수 있습니다.");
        }

        // 날짜 범위 설정
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1); // 이번 달 1일
        }
        if (endDate == null) {
            endDate = LocalDate.now(); // 오늘
        }

        // 기록 조회
        List<WorkRecord> records = workRecordRepository.findByEmployeeAndWorkDateBetween(employee, startDate, endDate);

        // 통계 계산
        Long totalWorkDays = (long) records.stream()
                .map(WorkRecord::getWorkDate)
                .distinct()
                .count();

        Double totalWorkHours = records.stream()
                .map(workRecordMapper::calculateTotalWorkHours)
                .filter(hours -> hours != null)
                .reduce(0.0, Double::sum);

        Double totalActualWorkHours = records.stream()
                .map(workRecordMapper::calculateActualWorkHours)
                .filter(hours -> hours != null)
                .reduce(0.0, Double::sum);

        Double averageWorkHoursPerDay = totalWorkDays > 0 ? totalActualWorkHours / totalWorkDays : 0.0;

        return EmployeeStatisticsResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .startDate(startDate)
                .endDate(endDate)
                .totalWorkDays(totalWorkDays)
                .totalWorkHours(totalWorkHours)
                .totalActualWorkHours(totalActualWorkHours)
                .averageWorkHoursPerDay(averageWorkHoursPerDay)
                .build();
    }

    // 월별 통계
    @Transactional(readOnly = true)
    public MonthlyStatisticsResponse getMonthlyStatistics(String employerEmail, Integer year, Integer month) {
        Employer employer = employerRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        // 기본값: 이번 달
        if (year == null || month == null) {
            YearMonth currentMonth = YearMonth.now();
            year = currentMonth.getYear();
            month = currentMonth.getMonthValue();
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 전체 직원
        List<Employee> employees = employeeRepository.findByEmployer(employer);

        // 해당 월의 모든 기록
        List<WorkRecord> monthRecords = employees.stream()
                .flatMap(emp -> workRecordRepository.findByEmployeeAndWorkDateBetween(emp, startDate, endDate).stream())
                .toList();

        // 전체 통계
        Long totalWorkDays = (long) monthRecords.stream()
                .map(WorkRecord::getWorkDate)
                .distinct()
                .count();

        Double totalWorkHours = monthRecords.stream()
                .map(workRecordMapper::calculateTotalWorkHours)
                .filter(hours -> hours != null)
                .reduce(0.0, Double::sum);

        Double totalActualWorkHours = monthRecords.stream()
                .map(workRecordMapper::calculateActualWorkHours)
                .filter(hours -> hours != null)
                .reduce(0.0, Double::sum);

        Long totalEmployeeCount = (long) employees.stream()
                .filter(emp -> monthRecords.stream().anyMatch(record -> record.getEmployee().getId().equals(emp.getId())))
                .count();

        // 직원별 요약
        List<EmployeeMonthlySummary> employeeSummaries = employees.stream()
                .map(emp -> {
                    List<WorkRecord> empRecords = monthRecords.stream()
                            .filter(record -> record.getEmployee().getId().equals(emp.getId()))
                            .toList();

                    if (empRecords.isEmpty()) {
                        return null;
                    }

                    Long workDays = (long) empRecords.stream()
                            .map(WorkRecord::getWorkDate)
                            .distinct()
                            .count();

                    Double empTotalWorkHours = empRecords.stream()
                            .map(workRecordMapper::calculateTotalWorkHours)
                            .filter(hours -> hours != null)
                            .reduce(0.0, Double::sum);

                    Double empTotalActualWorkHours = empRecords.stream()
                            .map(workRecordMapper::calculateActualWorkHours)
                            .filter(hours -> hours != null)
                            .reduce(0.0, Double::sum);

                    Double avgHours = workDays > 0 ? empTotalActualWorkHours / workDays : 0.0;

                    return EmployeeMonthlySummary.builder()
                            .employeeId(emp.getId())
                            .employeeName(emp.getName())
                            .workDays(workDays)
                            .totalWorkHours(empTotalWorkHours)
                            .totalActualWorkHours(empTotalActualWorkHours)
                            .averageWorkHoursPerDay(avgHours)
                            .build();
                })
                .filter(summary -> summary != null)
                .collect(Collectors.toList());

        return MonthlyStatisticsResponse.builder()
                .year(year)
                .month(month)
                .totalWorkDays(totalWorkDays)
                .totalWorkHours(totalWorkHours)
                .totalActualWorkHours(totalActualWorkHours)
                .totalEmployeeCount(totalEmployeeCount)
                .employeeSummaries(employeeSummaries)
                .build();
    }

    // 주간 통계
    @Transactional(readOnly = true)
    public WeeklyStatisticsResponse getWeeklyStatistics(String employerEmail, LocalDate date) {
        Employer employer = employerRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        // 기본값: 오늘 날짜
        if (date == null) {
            date = LocalDate.now();
        }

        // 주간 시작 요일 가져오기 (1=월요일, 7=일요일)
        Integer weekStartDay = employer.getWeekStartDay();
        if (weekStartDay == null) {
            weekStartDay = 1; // 기본값: 월요일
        }

        // 해당 날짜가 속한 주간의 시작일과 종료일 계산
        LocalDate weekStartDate = calculateWeekStart(date, weekStartDay);
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        // 전체 직원
        List<Employee> employees = employeeRepository.findByEmployer(employer);

        // 해당 주간의 모든 기록
        List<WorkRecord> weekRecords = employees.stream()
                .flatMap(emp -> workRecordRepository.findByEmployeeAndWorkDateBetween(emp, weekStartDate, weekEndDate).stream())
                .toList();

        // 전체 통계
        Long totalWorkDays = (long) weekRecords.stream()
                .map(WorkRecord::getWorkDate)
                .distinct()
                .count();

        Double totalWorkHours = weekRecords.stream()
                .map(workRecordMapper::calculateTotalWorkHours)
                .filter(hours -> hours != null)
                .reduce(0.0, Double::sum);

        Double totalActualWorkHours = weekRecords.stream()
                .map(workRecordMapper::calculateActualWorkHours)
                .filter(hours -> hours != null)
                .reduce(0.0, Double::sum);

        Long totalEmployeeCount = (long) employees.stream()
                .filter(emp -> weekRecords.stream().anyMatch(record -> record.getEmployee().getId().equals(emp.getId())))
                .count();

        // 직원별 요약
        List<EmployeeWeeklySummary> employeeSummaries = employees.stream()
                .map(emp -> {
                    List<WorkRecord> empRecords = weekRecords.stream()
                            .filter(record -> record.getEmployee().getId().equals(emp.getId()))
                            .toList();

                    if (empRecords.isEmpty()) {
                        return null;
                    }

                    Long workDays = (long) empRecords.stream()
                            .map(WorkRecord::getWorkDate)
                            .distinct()
                            .count();

                    Double empTotalWorkHours = empRecords.stream()
                            .map(workRecordMapper::calculateTotalWorkHours)
                            .filter(hours -> hours != null)
                            .reduce(0.0, Double::sum);

                    Double empTotalActualWorkHours = empRecords.stream()
                            .map(workRecordMapper::calculateActualWorkHours)
                            .filter(hours -> hours != null)
                            .reduce(0.0, Double::sum);

                    Double avgHours = workDays > 0 ? empTotalActualWorkHours / workDays : 0.0;

                    return EmployeeWeeklySummary.builder()
                            .employeeId(emp.getId())
                            .employeeName(emp.getName())
                            .workDays(workDays)
                            .totalWorkHours(empTotalWorkHours)
                            .totalActualWorkHours(empTotalActualWorkHours)
                            .averageWorkHoursPerDay(avgHours)
                            .build();
                })
                .filter(summary -> summary != null)
                .collect(Collectors.toList());

        return WeeklyStatisticsResponse.builder()
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .weekStartDay(weekStartDay)
                .totalWorkDays(totalWorkDays)
                .totalWorkHours(totalWorkHours)
                .totalActualWorkHours(totalActualWorkHours)
                .totalEmployeeCount(totalEmployeeCount)
                .employeeSummaries(employeeSummaries)
                .build();
    }

    // 주간 시작일 계산 (주어진 날짜가 속한 주간의 시작일)
    private LocalDate calculateWeekStart(LocalDate date, Integer weekStartDay) {
        // Java의 DayOfWeek: MONDAY=1, TUESDAY=2, ..., SUNDAY=7
        int currentDayOfWeek = date.getDayOfWeek().getValue();
        
        // weekStartDay를 Java DayOfWeek 형식으로 변환
        // 사용자 입력: 1=월요일, 2=화요일, ..., 7=일요일
        // Java DayOfWeek: 1=월요일, 2=화요일, ..., 7=일요일
        // 따라서 그대로 사용 가능
        
        // 현재 날짜의 요일과 주간 시작 요일의 차이 계산
        int daysToSubtract = (currentDayOfWeek - weekStartDay + 7) % 7;
        
        return date.minusDays(daysToSubtract);
    }
}

