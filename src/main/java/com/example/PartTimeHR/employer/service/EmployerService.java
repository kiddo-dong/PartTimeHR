package com.example.PartTimeHR.employer.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.mapper.EmployeeMapper;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.dto.*;
import com.example.PartTimeHR.employer.dto.UpdateEmployerRequest;
import com.example.PartTimeHR.employer.mapper.EmployerMapper;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.paypolicy.service.PayPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployerService {

    private final EmployerRepository employerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployerMapper employerMapper;
    private final EmployeeMapper employeeMapper;
    private final PayPolicyService payPolicyService;

    // 사장님이 직원 등록
    @Transactional
    public void registerEmployee(String email, RegisterEmployeeRequest request) {
        // 사장님 조회
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        // 이메일 중복 검사
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 확인 검사
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // Employee 생성
        Employee employee = Employee.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .employer(employer)
                .payPolicy(payPolicyService.getDefaultPolicy(employer))
                .role(Role.ROLE_EMPLOYEE)
                .build();

        // 저장
        employeeRepository.save(employee);
    }

    // 현재 로그인한 사장님 정보 조회
    @Transactional(readOnly = true)
    public EmployerInfoResponse getMyInfo(String email) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return employerMapper.toInfoResponse(employer);
    }

    // 사장님의 직원 목록 조회
    @Transactional(readOnly = true)
    public List<EmployeeListResponse> getMyEmployees(String email) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        List<Employee> employees = employeeRepository.findByEmployer(employer);

        return employees.stream()
                .map(employeeMapper::toListResponse)
                .toList();
    }

    // 사장님 정보 수정
    @Transactional
    public EmployerInfoResponse updateEmployer(String email, UpdateEmployerRequest request) {
        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        // 이름 수정
        if (request.getName() != null && !request.getName().isBlank()) {
            employer.setName(request.getName());
        }

        // 전화번호 수정
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            employer.setPhone(request.getPhone());
        }

        // 가게 이름 수정
        if (request.getStoreName() != null && !request.getStoreName().isBlank()) {
            employer.setStoreName(request.getStoreName());
        }

        // 주간 시작 요일 수정
        if (request.getWeekStartDay() != null) {
            if (request.getWeekStartDay() < 1 || request.getWeekStartDay() > 7) {
                throw new IllegalArgumentException("주간 시작 요일은 1(월요일)부터 7(일요일)까지입니다.");
            }
            employer.setWeekStartDay(request.getWeekStartDay());
        }

        // 주휴수당 적용 여부 수정
        if (request.getWeeklyPayApplicable() != null) {
            employer.setWeeklyPayApplicable(request.getWeeklyPayApplicable());
        }

        // 비밀번호 수정
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("비밀번호를 변경하려면 현재 비밀번호를 입력해주세요.");
            }

            if (!passwordEncoder.matches(request.getCurrentPassword(), employer.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            if (!request.getPassword().equals(request.getPasswordConfirm())) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }

            employer.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        employerRepository.save(employer);

        return employerMapper.toInfoResponse(employer);
    }


}
