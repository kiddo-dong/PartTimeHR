package com.example.PartTimeHR.mail.domain;

import org.springframework.security.core.AuthenticationException;

public class EmailNotVerifiedException extends AuthenticationException {

    public EmailNotVerifiedException() {
        super("이메일 인증이 필요합니다.");
    }
}
