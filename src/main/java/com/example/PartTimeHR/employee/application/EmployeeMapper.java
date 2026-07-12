package com.example.PartTimeHR.employee.application;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.presentation.dto.EmployeeInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "payPolicy.jobTitle", target = "jobTitle")
    @Mapping(source = "payPolicy.hourlyWage", target = "hourlyWage")
    EmployeeInfoResponse toInfoResponse(Employee employee);

    List<EmployeeInfoResponse> toInfoResponseList(List<Employee> employees);
}
