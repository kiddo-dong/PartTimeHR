package com.example.PartTimeHR.employee.application;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.domain.EmployeeAccessDeniedException;
import com.example.PartTimeHR.employee.domain.EmployeeEmailDuplicatesException;
import com.example.PartTimeHR.employee.domain.EmployeeNotFoundException;
import com.example.PartTimeHR.employee.domain.EmployeeRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.domain.StoreNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 나중에 PK 기준 조회 보안 향 (PK 위조로 검색 가능)
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

    public void checkEmployeeEmailDuplicates(Long storeId, String email) {
        if(employeeRepository.existsByStore_IdAndEmail(storeId, email)){
            throw new EmployeeEmailDuplicatesException();
        }
    }

}
