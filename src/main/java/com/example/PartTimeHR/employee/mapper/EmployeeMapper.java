package com.example.PartTimeHR.employee.mapper;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.dto.UpdateEmployeeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMapper {

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "payPolicy.jobTitle", target = "jobTitle")
    @Mapping(source = "payPolicy.hourlyWage", target = "hourlyWage")
    EmployeeInfoResponse toInfoResponse(Employee employee);

    List<EmployeeInfoResponse> toInfoResponseList(List<Employee> employees);

        void updateEmployeeFromDto(UpdateEmployeeRequest dto, @MappingTarget Employee entity);
}