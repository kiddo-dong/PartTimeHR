package com.example.PartTimeHR.schedule.exception;

public class DuplicateScheduleException extends RuntimeException {
    public DuplicateScheduleException() {
        super("근무 시간이 겹치는 출근 시간이 존재합니다.");
    }
}
