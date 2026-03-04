package com.example.PartTimeHR.attendance.domain;

import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.workrecord.domain.WorkRecord;

public class Attendance {

    private final Schedule schedule;
    private final WorkRecord workRecord;

    public Attendance(Schedule schedule, WorkRecord workRecord) {
        this.schedule = schedule;
        this.workRecord = workRecord;
    }

    public AttendanceStatus evaluate() {

        if (schedule != null && workRecord == null) {
            return AttendanceStatus.ABSENT;
        }

        if (schedule == null && workRecord != null) {
            return AttendanceStatus.UNSCHEDULED;
        }

        if (schedule != null && workRecord != null) {
            return AttendanceStatus.WORKED;
        }

        throw new IllegalStateException("판정할 수 없는 상태입니다.");
    }

    public int calculateDailyPay() {
        if (workRecord == null) return 0;
        return workRecord.calculateDailyPay();
    }
}