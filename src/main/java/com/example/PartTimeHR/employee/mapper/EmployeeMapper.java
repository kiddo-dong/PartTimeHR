package com.example.PartTimeHR.employee.mapper;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    EmployeeInfoResponse toInfoResponse(Employee employee);
}
