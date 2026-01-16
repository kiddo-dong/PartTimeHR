package com.example.PartTimeHR.employee.mapper;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeInfoResponse toInfoResponse(Employee employee);

    List<EmployeeInfoResponse> toInfoResponseList(List<Employee> employees);
}
