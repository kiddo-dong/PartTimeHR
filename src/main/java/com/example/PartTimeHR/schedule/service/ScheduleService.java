package com.example.PartTimeHR.schedule.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.exception.EmployeeAccessDeniedException;
import com.example.PartTimeHR.employee.exception.EmployeeNotFoundException;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.dto.CreateScheduleRequest;
import com.example.PartTimeHR.schedule.dto.ScheduleResponse;
import com.example.PartTimeHR.schedule.mapper.ScheduleMapper;
import com.example.PartTimeHR.schedule.repository.ScheduleRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.service.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final StoreAccessService storeAccessService;
    private final EmployeeRepository employeeRepository;
    private final ScheduleRepository scheduleRepository;

    private final ScheduleMapper mapper = ScheduleMapper.INSTANCE;

    // 스케줄 생성
    public void createSchedule(Long employerId, Long storeId, Long employeeId, CreateScheduleRequest request) {

        // 사장 소유 가게 검증
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 직원 조회 및 소속 검증
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(EmployeeNotFoundException::new);

        if (!employee.getStore().getId().equals(store.getId())) {
            throw new EmployeeAccessDeniedException();
        }

        // 스케줄 저장
        Schedule schedule = Schedule.builder()
                .store(store)
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .workingDays(request.getWorkingDays())
                .build();

        scheduleRepository.save(schedule);
    }

    // 특정 직원 스케줄 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getEmployeeSchedules(Long employerId, Long storeId, Long employeeId) {

        Store store = storeAccessService.getMyStore(storeId, employerId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(EmployeeNotFoundException::new);

        if (!employee.getStore().getId().equals(store.getId())) {
            throw new EmployeeAccessDeniedException();
        }

        return scheduleRepository.findByEmployeeId(employeeId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // 가게 전체 스케줄 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getStoreSchedules(Long employerId, Long storeId) {

        Store store = storeAccessService.getMyStore(storeId, employerId);

        return scheduleRepository.findByStoreId(storeId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

}
