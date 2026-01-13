package com.example.PartTimeHR.employee.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.dto.CreateEmployeeRequest;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.exception.PasswordMismatchException;
import com.example.PartTimeHR.employee.mapper.EmployeeMapper;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.repository.PayPolicyRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.exception.StoreAccessDeniedException;
import com.example.PartTimeHR.store.exception.StoreNotFoundException;
import com.example.PartTimeHR.store.repository.StoreRepository;
import com.example.PartTimeHR.employer.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final PayPolicyRepository payPolicyRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmployeeInfoResponse createEmployee(CreateEmployeeRequest request, Long employerId) {

        // 매장 조회
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(StoreNotFoundException::new);

        // 사장 소유 확인
        if (!store.getEmployer().getId().equals(employerId)) {
            throw new StoreAccessDeniedException();
        }

        // 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new PasswordMismatchException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // PayPolicy 결정
        PayPolicy policy;
        if (request.getPayPolicyId() != null) {
            policy = payPolicyRepository.findById(request.getPayPolicyId())
                    .orElseThrow(() -> new RuntimeException("해당 급여 정책이 없습니다."));
            if (!policy.getStore().getId().equals(store.getId())) {
                throw new StoreAccessDeniedException();
            }
        } else {
            policy = payPolicyRepository.findByStoreIdAndIsDefaultTrue(store.getId())
                    .orElseThrow(() -> new RuntimeException("기본 급여 정책이 없습니다."));
        }

        // Employee 생성
        Employee employee = Employee.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .store(store)
                .payPolicy(policy)
                .role(Role.ROLE_EMPLOYEE)
                .build();

        Employee saved = employeeRepository.save(employee);

        // DTO 변환
        EmployeeInfoResponse response = employeeMapper.toInfoResponse(saved);

        // 응답
        response = new EmployeeInfoResponse(
                response.getId(),
                response.getEmail(),
                response.getName(),
                response.getPhone(),
                response.getStoreId(),
                response.getStoreName(),
                policy.getJobTitle(),
                policy.getHourlyWage()
        );

        return response;
    }
}
