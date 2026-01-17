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
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.store.service.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PayPolicyRepository payPolicyRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final StoreAccessService storeAccessService;
    private final EmployeeAccessService employeeAccessService;

    // 직원 생성
    @Transactional
    public EmployeeInfoResponse createEmployee(Long storeId, CreateEmployeeRequest request, Long employerId) {

        // 매장 조회
        storeAccessService.findStore(employerId);

        // 사장 소유 확인
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 해당 매장에 직원의 이메일이 중복인지 확인
        employeeAccessService.checkEmployeeEmailDuplicates(store.getId(), request.getEmail());

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
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .store(store)
                .payPolicy(policy)
                .role(Role.ROLE_EMPLOYEE)
                .build();

        Employee saved = employeeRepository.save(employee);

        EmployeeInfoResponse response = employeeMapper.toInfoResponse(saved);

        // 응답
        return response;
    }

    // 전체 직원 조회
    @Transactional(readOnly = true)
    public List<EmployeeInfoResponse> getAllEmployees(Long employerId, Long storeId) {

        // 조회 가능한 가게인지 여부
        Store store = storeAccessService.getMyStore(storeId, employerId);

        List<Employee> employees = employeeRepository.findByStoreId(storeId);
        List<EmployeeInfoResponse> responses = employeeMapper.toInfoResponseList(employees);

        return responses;
    }

    @Transactional(readOnly = true)
    public EmployeeInfoResponse getEmployee(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        // 사장 소유 가게 검증 + 조회
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 해당 가게 소속 직원 검증 + 조회
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

        EmployeeInfoResponse response = employeeMapper.toInfoResponse(employee);
        return response;
    }

}
