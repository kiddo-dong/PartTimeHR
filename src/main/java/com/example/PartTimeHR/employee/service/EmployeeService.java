package com.example.PartTimeHR.employee.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.dto.EmployeeLoginRequest;
import com.example.PartTimeHR.employee.dto.EmployeeSignupRequest;
import com.example.PartTimeHR.employee.mapper.EmployeeMapper;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmployeeMapper employeeMapper;

    // signup logic
    @Transactional
    public void signup(EmployeeSignupRequest request) {

        // 이메일 중복 검사
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 확인 검사
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // Employer 존재 확인
        Employer employer = employerRepository.findById(request.getEmployerId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사장님입니다."));

        // Employee 생성
        Employee employee = Employee.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .employer(employer)
                .role(Role.ROLE_EMPLOYEE)
                .build();

        // 저장
        employeeRepository.save(employee);
    }

    // login logic
    @Transactional(readOnly = true)
    public String login(EmployeeLoginRequest request) {

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("비밀번호 불일치");
        }

        // JWT 발급
        return jwtTokenProvider.createToken(
                employee.getEmail(),
                employee.getRole().name()
        );
    }

    // 현재 로그인한 직원 정보 조회
    @Transactional(readOnly = true)
    public EmployeeInfoResponse getMyInfo(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return employeeMapper.toInfoResponse(employee);
    }
}

