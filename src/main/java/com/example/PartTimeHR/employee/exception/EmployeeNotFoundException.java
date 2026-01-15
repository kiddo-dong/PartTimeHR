package com.example.PartTimeHR.employee.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException() {
        super("직원을 찾을 수 없습니다.");
    }
}
