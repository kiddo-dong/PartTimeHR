package com.example.PartTimeHR.employee.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.exception.EmployeeAccessDeniedException;
import com.example.PartTimeHR.employee.exception.EmployeeNotFoundException;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.store.domain.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeAccessService {

    private final EmployeeRepository employeeRepository;

    // 특정 가게에 소속된 직원 단건 접근
    public Employee getEmployee(Long employeeId, Store store) {
        return employeeRepository
                .findByIdAndStore(employeeId, store)
                .orElseThrow(EmployeeAccessDeniedException::new);
    }

    // 직원 존재 여부만 확인 (내부용)
    public Employee getEmployeeOrThrow(Long employeeId) {
        return employeeRepository
                .findById(employeeId)
                .orElseThrow(EmployeeNotFoundException::new);
    }
}
