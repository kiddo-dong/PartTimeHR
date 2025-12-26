package com.example.PartTimeHR.employer.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.mapper.EmployeeMapper;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.dto.*;
import com.example.PartTimeHR.employer.mapper.EmployerMapper;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.global.jwt.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final EmployerMapper employerMapper;
    private final EmployeeMapper employeeMapper;

    // signup logic
    @Transactional
    public void signup(EmployerSignupRequest request) {

        // 이메일 중복 검사
        if (employerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 확인 검사
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // Employer 생성
        Employer employer = Employer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .storeName(request.getStoreName())
                .role(Role.ROLE_EMPLOYER)
                .build();

        // 저장
        employerRepository.save(employer);
    }

    // login logic
    @Transactional(readOnly = true)
    public String login(EmployerLoginRequest request) {

        Employer employer = employerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), employer.getPassword())) {
            throw new IllegalArgumentException("비밀번호 불일치");
        }

        // JWT 발급
        return jwtTokenProvider.createToken(
                employer.getEmail(),
                employer.getRole().name()
        );
    }

    // 사장님이 직원 등록
    @Transactional
    public void registerEmployee(String employerEmail, RegisterEmployeeRequest request) {
        // 사장님 조회
        Employer employer = employerRepository.findByEmail(employerEmail)
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
    public List<EmployeeListResponse> getMyEmployees(String employerEmail) {
        Employer employer = employerRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        List<Employee> employees = employeeRepository.findByEmployer(employer);

        return employees.stream()
                .map(employeeMapper::toListResponse)
                .toList();
    }

}
