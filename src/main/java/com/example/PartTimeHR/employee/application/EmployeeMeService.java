package com.example.PartTimeHR.employee.application;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.presentation.dto.EmployeeInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeMeService {

    private final EmployeeAccessService employeeAccessService;
    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public EmployeeInfoResponse getEmployeeInfo(Long employeeId){
        Employee employee = employeeAccessService.getEmployeeOrThrow(employeeId);

        return employeeMapper.toInfoResponse(employee);
    }
}
