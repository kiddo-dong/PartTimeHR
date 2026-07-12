package com.example.PartTimeHR.employer.application;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.presentation.dto.EmployerInfoResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployerMapper {

    EmployerInfoResponse toInfoResponse(Employer employer);
}
