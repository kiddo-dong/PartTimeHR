package com.example.PartTimeHR.schedule.mapper;

import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.schedule.dto.ScheduleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ScheduleMapper {

    ScheduleMapper INSTANCE = Mappers.getMapper(ScheduleMapper.class);

    ScheduleResponse toResponse(Schedule schedule);

}
