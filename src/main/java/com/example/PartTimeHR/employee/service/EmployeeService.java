package com.example.PartTimeHR.employee.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.dto.EmployeeLoginRequest;
import com.example.PartTimeHR.employee.dto.UpdateEmployeeRequest;
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

    // 직원 회원가입은 제거됨 - 사장님이 POST /api/employers/employees로 직원을 등록
    // 이 메서드는 더 이상 사용되지 않음

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

    // 직원 정보 수정
    @Transactional
    public EmployeeInfoResponse updateEmployee(String email, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));

        // 이름 수정
        if (request.getName() != null && !request.getName().isBlank()) {
            employee.setName(request.getName());
        }

        // 전화번호 수정
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            employee.setPhone(request.getPhone());
        }

        // 비밀번호 수정
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            // 현재 비밀번호 확인
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("비밀번호를 변경하려면 현재 비밀번호를 입력해주세요.");
            }

            // 현재 비밀번호 검증
            if (!passwordEncoder.matches(request.getCurrentPassword(), employee.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호 확인
            if (!request.getPassword().equals(request.getPasswordConfirm())) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }

            // 비밀번호 변경
            employee.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        employeeRepository.save(employee);

        return employeeMapper.toInfoResponse(employee);
    }
}

