package com.example.PartTimeHR.analysis.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.util.EnumSet;

@Getter
public class DailyWorkStatus {

    private final LocalDate date;

    private final boolean scheduled;   // 근무 예정일 여부
    private final boolean worked;      // 실제 출근 여부

    private final AttendanceStatus attendanceStatus; // NORMAL / ABSENT / LATE

    private int scheduledMinutes;
    private final int workedMinutes;   // 실근무 시간 (분)

    private final EnumSet<WorkFlag> workFlags; // OVERTIME 등

    private DailyWorkStatus(
            LocalDate date,
            boolean scheduled,
            boolean worked,
            AttendanceStatus attendanceStatus,
            int workedMinutes,
            EnumSet<WorkFlag> workFlags
    ) {
        this.date = date;
        this.scheduled = scheduled;
        this.worked = worked;
        this.attendanceStatus = attendanceStatus;
        this.workedMinutes = workedMinutes;
        this.workFlags = workFlags;
    }

    // === 정적 팩토리 ===
    public static DailyWorkStatus of(
            LocalDate date,
            boolean scheduled,
            boolean worked,
            AttendanceStatus attendanceStatus,
            int workedMinutes,
            EnumSet<WorkFlag> workFlags
    ) {
        return new DailyWorkStatus(
                date,
                scheduled,
                worked,
                attendanceStatus,
                workedMinutes,
                workFlags == null ? EnumSet.noneOf(WorkFlag.class) : workFlags
        );
    }
    public boolean hasFlag(WorkFlag flag) {
        return workFlags.contains(flag);
    }

    public boolean isAbsent() {
        return attendanceStatus == AttendanceStatus.ABSENT;
    }
}
