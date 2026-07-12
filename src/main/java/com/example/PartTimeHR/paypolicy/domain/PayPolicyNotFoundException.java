package com.example.PartTimeHR.paypolicy.domain;

public class PayPolicyNotFoundException extends RuntimeException {
    public PayPolicyNotFoundException() {
        super("해당 급여 정책을 찾을 수 없습니다.");
    }
}
