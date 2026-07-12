package com.example.PartTimeHR.workrecord.application;

import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.presentation.dto.WorkRecordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkRecordMapper {

    // status/집계/휴게 시각은 전부 엔티티의 파생 게터에서 나온다
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "status", expression = "java(workRecord.getStatus().name())")
    @Mapping(target = "totalWorkMinutes", expression = "java(workRecord.getTotalWorkMinutes())")
    @Mapping(target = "breakMinutes", expression = "java(workRecord.getBreakMinutes())")
    @Mapping(target = "actualWorkMinutes", expression = "java(workRecord.getActualWorkMinutes())")
    WorkRecordResponse toResponse(WorkRecord workRecord);
}
