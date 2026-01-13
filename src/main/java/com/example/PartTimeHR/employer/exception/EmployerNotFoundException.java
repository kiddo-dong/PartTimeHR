package com.example.PartTimeHR.employer.exception;

public class EmployerNotFoundException extends RuntimeException {

    public EmployerNotFoundException() {
        super("사장 정보를 찾을 수 없습니다.");
    }
}

