package com.example.PartTimeHR.paypolicy.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.dto.UpdatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.dto.UpdatePayPolicyResponse;
import com.example.PartTimeHR.paypolicy.repository.PayPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayPolicyService {

    private final PayPolicyRepository payPolicyRepository;
    private final EmployerRepository employerRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public PayPolicy getDefaultPolicy(Employer employer) {
        return payPolicyRepository.findByEmployerAndIsDefaultTrue(employer)
                .orElseThrow(() -> new IllegalStateException("기본 급여 정책이 없습니다."));
    }

    public PayPolicy createPolicy(PayPolicy policy) {
        return payPolicyRepository.save(policy);
    }

    @Transactional
    public UpdatePayPolicyResponse updateEmployeePolicy(Long employeeId, String employerEmail, UpdatePayPolicyRequest request) {
        // 사장님 검증
        Employer employer = employerRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사장님을 찾을 수 없습니다."));

        // 직원 검증
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));

        if(!employee.getEmployer().getId().equals(employer.getId())) {
            throw new IllegalArgumentException("자신의 직원만 수정 가능합니다.");
        }

        // 새 정책 생성
        PayPolicy newPolicy = PayPolicy.builder()
                .employer(employer)
                .jobTitle(request.getJobTitle())
                .hourlyWage(request.getHourlyWage())
                .isDefault(false)
                .build();

        payPolicyRepository.save(newPolicy);

        // 직원에 새 정책 연결
        employee.setPayPolicy(newPolicy);

        return UpdatePayPolicyResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .jobTitle(newPolicy.getJobTitle())
                .hourlyWage(newPolicy.getHourlyWage())
                .build();
    }
}