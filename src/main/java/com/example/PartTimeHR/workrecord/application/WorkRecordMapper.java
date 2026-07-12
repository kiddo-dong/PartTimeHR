package com.example.PartTimeHR.workrecord.application;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.presentation.dto.CreateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.presentation.dto.UpdateWorkRecordRequest;
import com.example.PartTimeHR.workrecord.presentation.dto.WorkRecordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkRecordMapper {

    // ===== 기존 response 변환 =====
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "status", expression = "java(workRecord.getStatus().name())")
    @Mapping(target = "totalWorkMinutes", expression = "java(workRecord.getTotalWorkMinutes())")
    @Mapping(target = "breakMinutes", expression = "java(workRecord.getBreakMinutes())")
    @Mapping(target = "actualWorkMinutes", expression = "java(workRecord.getActualWorkMinutes())")
    WorkRecordResponse toResponse(WorkRecord workRecord);

    // ===== 생성용 =====
    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "status", expression = "java(com.example.PartTimeHR.workrecord.domain.WorkStatus.IN_PROGRESS)")
    @Mapping(target = "totalBreakMinutes", constant = "0")
    @Mapping(target = "totalWorkedMinutes", constant = "0")
    @Mapping(target = "netWorkedMinutes", constant = "0")
    WorkRecord toEntity(CreateWorkRecordRequest request, Employee employee);

    // ===== 수정용 (덮어쓰기) =====
    void updateFromRequest(UpdateWorkRecordRequest request, @MappingTarget WorkRecord record);
}
