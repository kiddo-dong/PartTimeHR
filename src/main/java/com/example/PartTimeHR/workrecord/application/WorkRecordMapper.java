package com.example.PartTimeHR.workrecord.application;

import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.presentation.dto.UpdateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.presentation.dto.WorkRecordResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface WorkRecordMapper {

    // ===== response 변환 =====
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "status", expression = "java(workRecord.getStatus().name())")
    @Mapping(target = "totalWorkMinutes", expression = "java(workRecord.getTotalWorkMinutes())")
    @Mapping(target = "breakMinutes", expression = "java(workRecord.getBreakMinutes())")
    @Mapping(target = "actualWorkMinutes", expression = "java(workRecord.getActualWorkMinutes())")
    WorkRecordResponse toResponse(WorkRecord workRecord);

    // ===== 수정용 (부분 수정: null 필드는 기존 값 유지) =====
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(UpdateWorkRecordRequest request, @MappingTarget WorkRecord record);
}
