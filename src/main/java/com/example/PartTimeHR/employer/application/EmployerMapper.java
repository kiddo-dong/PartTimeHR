package com.example.PartTimeHR.employer.application;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.presentation.dto.EmployerInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployerMapper {

    @Mapping(source = "user.email", target = "email")
    EmployerInfoResponse toInfoResponse(Employer employer);
}
