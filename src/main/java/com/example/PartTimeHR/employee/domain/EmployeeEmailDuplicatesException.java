package com.example.PartTimeHR.employee.domain;

public class EmployeeEmailDuplicatesException extends RuntimeException {
    public EmployeeEmailDuplicatesException() {
        super("이미 사용 중인 이메일입니다.");
    }
}