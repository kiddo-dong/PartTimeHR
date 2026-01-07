package com.example.PartTimeHR.workrecord.mapper;

import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Duration;
import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkRecordMapper {

    WorkRecordMapper INSTANCE = Mappers.getMapper(WorkRecordMapper.class);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "status", expression = "java(workRecord.getStatus().name())")
    @Mapping(target = "totalWorkHours", expression = "java(calculateTotalWorkHours(workRecord))")
    @Mapping(target = "breakHours", expression = "java(calculateBreakHours(workRecord))")
    @Mapping(target = "actualWorkHours", expression = "java(calculateActualWorkHours(workRecord))")
    @Mapping(target = "appliedHourlyWage", source = "appliedHourlyWage")
    @Mapping(target = "appliedJobName", source = "appliedJobName")
    WorkRecordResponse toResponse(WorkRecord workRecord);

    List<WorkRecordResponse> toResponseList(List<WorkRecord> workRecordList);

    // 총 근무 시간 계산 (출근 ~ 퇴근)
    default Double calculateTotalWorkHours(WorkRecord workRecord) {
        if (workRecord.getClockOutTime() == null || workRecord.getClockInTime() == null) {
            return null;
        }
        Duration duration = Duration.between(workRecord.getClockInTime(), workRecord.getClockOutTime());
        return duration.toMinutes() / 60.0;
    }

    // 휴게 시간 계산
    default Double calculateBreakHours(WorkRecord workRecord) {
        if (workRecord.getBreakStartTime() == null || workRecord.getBreakEndTime() == null) {
            return 0.0;
        }
        Duration duration = Duration.between(workRecord.getBreakStartTime(), workRecord.getBreakEndTime());
        return duration.toMinutes() / 60.0;
    }

    // 실제 근무 시간 계산 (총 - 휴게)
    default Double calculateActualWorkHours(WorkRecord workRecord) {
        Double total = calculateTotalWorkHours(workRecord);
        Double breakHours = calculateBreakHours(workRecord);
        if (total == null) return null;
        return total - breakHours;
    }
}
