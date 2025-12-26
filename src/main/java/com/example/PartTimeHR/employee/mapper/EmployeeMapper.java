package com.example.PartTimeHR.employee.mapper;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employer.dto.EmployeeListResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    @Mapping(target = "role", expression = "java(employee.getRole().name())")
    @Mapping(target = "employerId", source = "employer.id")
    @Mapping(target = "employerName", source = "employer.name")
    @Mapping(target = "storeName", source = "employer.storeName")
    EmployeeInfoResponse toInfoResponse(Employee employee);

    // EmployeeListResponse에는 role 필드가 없으므로 자동으로 무시됨
    EmployeeListResponse toListResponse(Employee employee);
}