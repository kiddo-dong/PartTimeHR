package com.example.PartTimeHR.schedule.exception;

public class ScheduleAccessDeniedException extends RuntimeException {
    public ScheduleAccessDeniedException() {
        super("해당 매장에 대한 권한이 없습니다.");
    }
}
