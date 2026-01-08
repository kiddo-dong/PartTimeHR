package com.example.PartTimeHR.workrecord.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.exception.EmployeeNotFoundException;
import com.example.PartTimeHR.workrecord.exception.WorkRecordNotFoundException;
import com.example.PartTimeHR.workrecord.mapper.WorkRecordMapper;
import com.example.PartTimeHR.workrecord.repository.EmployeeWorkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;


// 추후 개발(Statistics 통계 도메인)에서 재사용 가능
@Service
@RequiredArgsConstructor
public class EmployeeWorkRecordQueryService {

    private final EmployeeWorkRecordRepository employeeWorkRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkRecordMapper workRecordMapper;

    // 오늘 근무 기록 조회 (/today)
    @Transactional(readOnly = true)
    public List<WorkRecordResponse> today(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(EmployeeNotFoundException::new);

        // KST 기준 오늘
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<WorkRecord> workRecordList = employeeWorkRecordRepository.findByEmployeeAndWorkDate(employee, today);

        if (workRecordList.isEmpty()) {
            throw new WorkRecordNotFoundException("오늘 출근한 기록이 없습니다.");
        }

        return workRecordMapper.toResponseList(workRecordList);
    }

    // 주별 조회 (/week) (ISO 기준: 주 시작일은 Monday)
    @Transactional(readOnly = true)
    public List<WorkRecordResponse> findByWeek(Long employeeId, int weekOffset) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("직원 없음"));

        // 직원 소속 사장님 조회
        Employer employer = employee.getEmployer();
        int weekStartDay = employer.getWeekStartDay(); // 1=월, 7=일

        // KST 기준 오늘
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 사장님 기준 주 시작일 계산
        DayOfWeek startDayOfWeek = DayOfWeek.of(weekStartDay);
        int diff = today.getDayOfWeek().getValue() - startDayOfWeek.getValue();
        if (diff < 0) diff += 7;
        LocalDate startOfWeek = today.minusDays(diff);

        // weekOffset 적용 (사장님 주간 시작 기준으로 일주일 계산)
        startOfWeek = startOfWeek.plusWeeks(weekOffset);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // 범위로 조회
        List<WorkRecordResponse> responses = findByPeriod(employeeId, startOfWeek, endOfWeek);

        if (responses.isEmpty()) {
            throw new WorkRecordNotFoundException("해당 주에 출근한 기록이 없습니다.");
        }

        return responses;
    }

    // 월별 조회
    @Transactional(readOnly = true)
    public List<WorkRecordResponse> findByMonth(Long employeeId, int monthOffset) {
        // 존재여부 확인
        boolean employeeExists = employeeRepository.existsById(employeeId);

        if (!employeeExists) {
            throw new IllegalArgumentException("존재하지 않는 사용자.");
        }

        // KST 기준 오늘
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 현재 연/월 기준으로 monthOffset 적용
        YearMonth targetMonth = YearMonth.from(today).plusMonths(monthOffset);

        LocalDate startOfMonth = targetMonth.atDay(1);
        LocalDate endOfMonth = targetMonth.atEndOfMonth();

        List<WorkRecordResponse> responses = findByPeriod(employeeId, startOfMonth, endOfMonth);

        if (responses.isEmpty()) {
            throw new WorkRecordNotFoundException("해당 월에 출근한 기록이 없습니다.");
        }

        return responses;
    }


    // 특정 기간 출근 기록(/period)
    @Transactional(readOnly = true)
    public List<WorkRecordResponse> findByPeriod(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        // 방어 로직
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 일은 종료 일보다 이후일 수 없습니다.");
        }

        List<WorkRecord> records =
                employeeWorkRecordRepository
                        .findByEmployeeIdAndWorkDateBetween(
                                employeeId,
                                startDate,
                                endDate
                        );
        return workRecordMapper.toResponseList(records);
    }
}