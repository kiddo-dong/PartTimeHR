package com.example.PartTimeHR.employer.mapper;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.dto.EmployerInfoResponse;
import com.example.PartTimeHR.employer.dto.UpdateEmployerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployerMapper {

    EmployerInfoResponse toInfoResponse(Employer employer);

    // 기존 엔티티 업데이트용
    void updateEmployerFromDto(UpdateEmployerRequest dto, @MappingTarget Employer entity);
}
