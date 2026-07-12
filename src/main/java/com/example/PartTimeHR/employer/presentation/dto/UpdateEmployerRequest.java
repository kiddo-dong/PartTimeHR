package com.example.PartTimeHR.employer.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployerRequest {
    private String name;
    private String phone;
    private String password;
    private String passwordConfirm;  // 비밀번호 확인용
}
