package com.example.PartTimeHR.schedule.mapper;

import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.dto.ScheduleCreateRequest;
import com.example.PartTimeHR.schedule.dto.ScheduleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "confirmed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Schedule toEntity(ScheduleCreateRequest request);

    @Mapping(source = "id", target = "scheduleId")
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.name", target = "employeeName")
    ScheduleResponse toResponse(Schedule schedule);
}
