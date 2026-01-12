package com.example.PartTimeHR.statistics.service;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.statistics.dto.EmployeeMonthWorkStatisticsResponse;
import com.example.PartTimeHR.statistics.dto.EmployeeWeekWorkStatisticsResponse;
import com.example.PartTimeHR.statistics.dto.EmployeeWorkStatisticsResponse;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.service.EmployerWorkRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final EmployerWorkRecordService employerWorkRecordService;
    private final EmployerRepository employerRepository;

    // 주간 통계
    public EmployeeWeekWorkStatisticsResponse getEmployeeWeekWorkStatistics(
            Long employerId,
            int offset
    ) {
        // 주간 근무 기록 조회
        List<WorkRecordResponse> records =
                employerWorkRecordService.findStoreWeek(employerId, offset);

        // 직원별 그룹핑
        Map<Long, List<WorkRecordResponse>> grouped =
                records.stream()
                        .collect(Collectors.groupingBy(WorkRecordResponse::getEmployeeId));

        // 사장 주휴수당 정책
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow();
        boolean weeklyPayApplicable = employer.isWeeklyPayApplicable();

        // 직원별 통계 계산
        List<EmployeeWorkStatisticsResponse> employeeStats =
                grouped.values().stream()
                        .map(list -> calculateEmployeeStatistics(list, weeklyPayApplicable))
                        .sorted(Comparator.comparing(EmployeeWorkStatisticsResponse::getName))
                        .toList();


        // 전체 합계
        long totalWorkMinutes = employeeStats.stream()
                .mapToLong(EmployeeWorkStatisticsResponse::getTotalWorkMinutes)
                .sum();

        long totalPay = employeeStats.stream()
                .mapToLong(EmployeeWorkStatisticsResponse::getTotalPay)
                .sum();

        // 주 시작 / 종료일 (사장님의 WeekStartDay기준)
        int weekStartDay = employer.getWeekStartDay();

        LocalDate baseDate = records.get(0).getWorkDate();
        LocalDate weekStart = calculateWeekStart(baseDate, weekStartDay);
        LocalDate weekEnd = weekStart.plusDays(6);

        // 7. 응답 DTO(Week)
        EmployeeWeekWorkStatisticsResponse response =
                new EmployeeWeekWorkStatisticsResponse();

        response.setWeekStartDate(weekStart);
        response.setWeekEndDate(weekEnd);
        response.setTotalWorkMinutes(totalWorkMinutes);
        response.setTotalPay(totalPay);
        response.setEmployeeWorkStatistics(employeeStats);

        return response;
    }

    // 월별 통계 계산
    // =====================
    public EmployeeMonthWorkStatisticsResponse getEmployeeMonthWorkStatistics(
            Long employerId,
            int offset
    ) {
        // 1. 월간 근무 기록 조회
        List<WorkRecordResponse> records =
                employerWorkRecordService.findStoreMonth(employerId, offset);

        // 2. 직원별 그룹핑
        Map<Long, List<WorkRecordResponse>> grouped =
                records.stream()
                        .collect(Collectors.groupingBy(WorkRecordResponse::getEmployeeId));

        // 사장 주휴수당 정책
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow();
        boolean weeklyPayApplicable = employer.isWeeklyPayApplicable();

        // 3. 직원별 통계 계산
        List<EmployeeWorkStatisticsResponse> employeeStats =
                grouped.values().stream()
                        .map(list -> calculateEmployeeStatistics(list, weeklyPayApplicable))
                        .sorted(Comparator.comparing(EmployeeWorkStatisticsResponse::getName))
                        .toList();


        // 4. 전체 합계
        long totalWorkMinutes = employeeStats.stream()
                .mapToLong(EmployeeWorkStatisticsResponse::getTotalWorkMinutes)
                .sum();


        long totalPay = employeeStats.stream()
                .mapToLong(EmployeeWorkStatisticsResponse::getTotalPay)
                .sum();

        // 5. 월 시작일 / 종료일 계산
        LocalDate baseDate = LocalDate.now().minusMonths(offset);
        LocalDate monthStart = baseDate.withDayOfMonth(1);
        LocalDate monthEnd = baseDate.withDayOfMonth(baseDate.lengthOfMonth());

        // 6. 응답 DTO 생성
        EmployeeMonthWorkStatisticsResponse response =
                new EmployeeMonthWorkStatisticsResponse();

        response.setMonthStartDate(monthStart);
        response.setMonthEndDate(monthEnd);
        response.setTotalWorkMinutes(totalWorkMinutes);
        response.setTotalPay(totalPay);
        response.setEmployeeWorkStatistics(employeeStats);

        return response;

    }



    // ====== 내부 메소드 ======
    // 직원 단위 계산 (주/월 공통 재사용)
    private EmployeeWorkStatisticsResponse calculateEmployeeStatistics(
            List<WorkRecordResponse> records,
            boolean weeklyPayApplicable
    ) {
        WorkRecordResponse first = records.get(0);

        long totalWorkMinutes = records.stream()
                .mapToLong(r -> Math.round(r.getActualWorkHours() * 60))
                .sum();

        long workDays = records.stream()
                .map(WorkRecordResponse::getWorkDate)
                .distinct()
                .count();

        int hourlyWage = first.getAppliedHourlyWage();
        long basePay = totalWorkMinutes * hourlyWage / 60;

        // 주휴 수당 추후 개선
        long weeklyAllowance = 0;
        if (weeklyPayApplicable) {
            weeklyAllowance = hourlyWage * 8;
        }


        return EmployeeWorkStatisticsResponse.builder()
                .name(first.getEmployeeName())
                .jobTitle(first.getAppliedJobName())
                .hourlyWage(hourlyWage)
                .workDays(workDays)
                .totalWorkMinutes(totalWorkMinutes)
                .totalPay(basePay + weeklyAllowance)
                .build();
    }

    // 사장님 주간 계산 메소드
    private LocalDate calculateWeekStart(LocalDate date, int weekStartDay) {
        int currentDay = date.getDayOfWeek().getValue(); // 1=월
        int diff = currentDay - weekStartDay;
        if (diff < 0) diff += 7;
        return date.minusDays(diff);
    }

}