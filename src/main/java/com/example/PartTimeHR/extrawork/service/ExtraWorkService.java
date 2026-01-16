package com.example.PartTimeHR.extrawork.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.exception.EmployeeAccessDeniedException;
import com.example.PartTimeHR.employee.exception.EmployeeNotFoundException;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.extrawork.domain.ExtraWork;
import com.example.PartTimeHR.extrawork.dto.CreateExtraWorkRequest;
import com.example.PartTimeHR.extrawork.repository.ExtraWorkRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.service.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExtraWorkService {

    private final StoreAccessService storeAccessService;
    private final EmployeeRepository employeeRepository;
    private final ExtraWorkRepository extraWorkRepository;

    @Transactional
    public void create(
            Long employerId,
            Long storeId,
            Long employeeId,
            CreateExtraWorkRequest request
    ) {
        // 사장 소유 가게 검증
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 직원 조회
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(EmployeeNotFoundException::new);

        // 직원 소속 가게 검증
        if (!employee.getStore().getId().equals(store.getId())) {
            throw new EmployeeAccessDeniedException();
        }

        ExtraWork extraWork = ExtraWork.builder()
                .store(store)
                .employee(employee)
                .workDate(request.getWorkDate())
                .reason(request.getReason())
                .build();

        extraWorkRepository.save(extraWork);
    }

}
