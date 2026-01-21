package com.example.PartTimeHR.workrecord.mapper;

import com.example.PartTimeHR.workrecord.domain.WorkRecord;
import com.example.PartTimeHR.workrecord.dto.WorkRecordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;
import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkRecordMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "status", expression = "java(workRecord.getStatus().name())")

    @Mapping(
            target = "totalWorkHours",
            expression = "java(calculateTotalWorkHours(workRecord))"
    )
    @Mapping(
            target = "breakHours",
            expression = "java(calculateBreakHours(workRecord))"
    )
    @Mapping(
            target = "actualWorkHours",
            expression = "java(calculateActualWorkHours(workRecord))"
    )

    WorkRecordResponse toResponse(WorkRecord workRecord);

    List<WorkRecordResponse> toResponseList(List<WorkRecord> workRecordList);

    //시간 계산 로직 (분 기준)
    //출근 ~ 퇴근
    default Double calculateTotalWorkHours(WorkRecord workRecord) {
        if (workRecord.getClockInTime() == null || workRecord.getClockOutTime() == null) {
            return null;
        }

        long minutes = Duration.between(
                workRecord.getClockInTime(),
                workRecord.getClockOutTime()
        ).toMinutes();

        return minutes / 60.0;
    }

    //휴게 시간
    default Double calculateBreakHours(WorkRecord workRecord) {
        if (workRecord.getBreakStartTime() == null || workRecord.getBreakEndTime() == null) {
            return 0.0;
        }

        long minutes = Duration.between(
                workRecord.getBreakStartTime(),
                workRecord.getBreakEndTime()
        ).toMinutes();

        return minutes / 60.0;
    }

    //실제 근무 시간
    default Double calculateActualWorkHours(WorkRecord workRecord) {
        Double total = calculateTotalWorkHours(workRecord);
        Double breakHours = calculateBreakHours(workRecord);

        if (total == null) {
            return null;
        }

        double actual = total - breakHours;
        return Math.max(actual, 0.0);
    }
}
