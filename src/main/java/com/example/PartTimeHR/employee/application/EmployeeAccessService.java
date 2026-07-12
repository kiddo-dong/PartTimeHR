package com.example.PartTimeHR.employee.application;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.domain.EmployeeAccessDeniedException;
import com.example.PartTimeHR.employee.domain.EmployeeEmailDuplicatesException;
import com.example.PartTimeHR.employee.domain.EmployeeNotFoundException;
import com.example.PartTimeHR.employee.domain.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.EmployerRepository;
import com.example.PartTimeHR.store.domain.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeAccessService {

    private final EmployeeRepository employeeRepository;
    private final EmployerRepository employerRepository;

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

    /**
     * 이메일 사용 가능 여부 검사.
     * employee.email은 DB 전역 unique이고 로그인도 이메일 하나로 이뤄지므로
     * 직원 전체 + 사장 계정까지 전역으로 검사한다.
     * (사장과 같은 이메일의 직원은 로그인 시 사장 계정이 먼저 조회되어 영영 로그인 불가)
     */
    public void checkEmailDuplicates(String email) {
        if (employeeRepository.existsByEmail(email) || employerRepository.existsByEmail(email)) {
            throw new EmployeeEmailDuplicatesException();
        }
    }

}
