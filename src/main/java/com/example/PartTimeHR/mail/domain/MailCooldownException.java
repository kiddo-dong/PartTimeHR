package com.example.PartTimeHR.mail.domain;

public class MailCooldownException extends RuntimeException {
    public MailCooldownException() {
        super("메일이 방금 발송되었습니다. 잠시 후 다시 시도해주세요.");
    }
}
