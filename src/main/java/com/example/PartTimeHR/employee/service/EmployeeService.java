package com.example.PartTimeHR.employee.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.mapper.EmployeeMapper;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.repository.PayPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PayPolicyRepository payPolicyRepository;
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public EmployeeInfoResponse getMyInfo(Long employeeId) {

        // 직원 조회
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("직원을 찾을 수 없습니다."));

        Employer employer = employee.getEmployer();

        // 직원별 정책 있으면 그걸 사용, 없으면 사장님 기본 정책
        PayPolicy payPolicy = null;

        if (employee.getPayPolicy() != null && employee.getPayPolicy().isActive()) {
            payPolicy = employee.getPayPolicy();
        } else {
            payPolicy = payPolicyRepository
                    .findByEmployerAndIsDefaultTrueAndActiveTrue(employer)
                    .orElseThrow(() -> new RuntimeException("적용 가능한 급여 정책이 없습니다."));
        }

        // DTO 변환
        EmployeeInfoResponse response = employeeMapper.toInfoResponse(employee);

        // 직원별 정책 정보 채우기
        return EmployeeInfoResponse.builder()
                .id(response.getId())
                .email(response.getEmail())
                .name(response.getName())
                .phone(response.getPhone())
                .role(response.getRole())
                .employerId(response.getEmployerId())
                .employerName(response.getEmployerName())
                .storeName(response.getStoreName())
                .jobTitle(payPolicy.getJobTitle())
                .hourlyWage(payPolicy.getHourlyWage())
                .createdAt(response.getCreatedAt())
                .build();
    }
}


