package com.example.PartTimeHR.employee.exception;

public class EmployeeAccessDeniedException extends RuntimeException {
    public EmployeeAccessDeniedException() {
        super("해당 직원에 대한 접근 권한이 없습니다.");
    }
}
