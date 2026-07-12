package com.example.PartTimeHR.employee.domain;

public class EmployeeEmailDuplicatesException extends RuntimeException {
    public EmployeeEmailDuplicatesException() {
        super("이미 매장에 존재하는 이메일입니다.");
    }
}