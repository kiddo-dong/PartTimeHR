package com.example.PartTimeHR.auth.domain;

// 매장 초대코드로 가입했지만 사장 승인 전인 계정의 로그인 시도
public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException() {
        super("승인 대기 중인 계정입니다. 사장님의 승인을 기다려주세요.");
    }
}
