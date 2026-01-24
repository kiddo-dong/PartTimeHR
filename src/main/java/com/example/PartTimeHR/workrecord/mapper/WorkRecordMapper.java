package com.example.PartTimeHR.workrecord.mapper;

import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkRecordMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "status",
            expression = "java(workRecord.getStatus().name())")

    @Mapping(target = "totalWorkMinutes",
            expression = "java(workRecord.getTotalWorkMinutes())")
    @Mapping(target = "breakMinutes",
            expression = "java(workRecord.getBreakMinutes())")
    @Mapping(target = "actualWorkMinutes",
            expression = "java(workRecord.getActualWorkMinutes())")
    WorkRecordResponse toResponse(WorkRecord workRecord);
}
