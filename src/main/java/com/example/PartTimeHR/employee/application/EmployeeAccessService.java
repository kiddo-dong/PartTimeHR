package com.example.PartTimeHR.employee.application;

import com.example.PartTimeHR.auth.domain.UserRepository;
import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.domain.EmployeeAccessDeniedException;
import com.example.PartTimeHR.employee.domain.EmployeeEmailDuplicatesException;
import com.example.PartTimeHR.employee.domain.EmployeeNotFoundException;
import com.example.PartTimeHR.employee.domain.EmployeeRepository;
import com.example.PartTimeHR.store.domain.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeAccessService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

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
     * 이메일은 User에서 전역 unique이고 로그인도 이메일 하나로 이뤄지므로
     * User 하나만 검사하면 사장/직원 전체에 대한 중복 검사가 끝난다.
     */
    public void checkEmailDuplicates(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmployeeEmailDuplicatesException();
        }
    }

}
