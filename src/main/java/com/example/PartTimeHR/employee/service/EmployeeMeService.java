package com.example.PartTimeHR.employee.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.mapper.EmployeeMapper;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeMeService {

    public final EmployeeRepository employeeRepository;
    public final EmployeeAccessService employeeAccessService;
    public final EmployeeMapper employeeMapper;

    public EmployeeInfoResponse getEmployeeInfo(Long employeeId){
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        EmployeeInfoResponse response = employeeMapper.toInfoResponse(employee);
        return response;
    }
}