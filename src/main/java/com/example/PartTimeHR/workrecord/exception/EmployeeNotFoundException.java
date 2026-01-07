package com.example.PartTimeHR.workrecord.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException() {
        super("사용자를 찾을 수 없습니다.");
    }
}
