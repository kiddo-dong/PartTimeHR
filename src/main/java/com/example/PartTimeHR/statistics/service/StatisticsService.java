package com.example.PartTimeHR.statistics.service;

import com.example.PartTimeHR.statistics.dto.EmployeeWeekWorkStatisticsResponse;
import com.example.PartTimeHR.statistics.dto.EmployeeWorkStatisticsResponse;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import com.example.PartTimeHR.workrecord.service.EmployerWorkRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final EmployerWorkRecordService employerWorkRecordService;

   /* public EmployeeWeekWorkStatisticsResponse getEmployeeWeekWorkStatistics(Long employerId, int offset) {
        List<WorkRecordResponse> workRecordResponse = employerWorkRecordService.findStoreWeek(employerId, offset);
    }*/

    // 주휴수당 계산 헬퍼 메소드

}