package com.example.PartTimeHR.employer.mapper;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.dto.EmployerInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EmployerMapper {

    @Mapping(target = "role", expression = "java(employer.getRole().name())")
    EmployerInfoResponse toInfoResponse(Employer employer);
}