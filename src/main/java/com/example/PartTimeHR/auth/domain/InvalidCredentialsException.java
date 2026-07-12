package com.example.PartTimeHR.auth.domain;

// 이메일이 없거나 비밀번호가 틀린 경우 (어느 쪽인지 구분해서 알려주지 않음)
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
