package com.example.PartTimeHR.employer.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.dto.EmployerInfoResponse;
import com.example.PartTimeHR.employer.dto.UpdateEmployerRequest;
import com.example.PartTimeHR.employer.mapper.EmployerMapper;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class EmployerService {
    private final EmployerRepository employerRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployerMapper employerMapper;

    // 본인 조회(Employer)
    @Transactional(readOnly = true)
    public EmployerInfoResponse getMyInfo(Long employerId) {

        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return employerMapper.toInfoResponse(employer);
    }

    // Update
    @Transactional
    public EmployerInfoResponse updateInfo(Long employerId, UpdateEmployerRequest request){

        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


        if (request.getPassword() != null && !request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }

        employerMapper.updateEmployerFromDto(request, employer);
        employerRepository.save(employer);

        return employerMapper.toInfoResponse(employer);
    }
}