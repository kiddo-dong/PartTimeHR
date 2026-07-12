package com.example.PartTimeHR.employee.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployeeRequest {
    private String name;
    private String phone;
    private String password;
    private String passwordConfirm; // 비밀번호 확인용
    private Long payPolicyId;       // 정책 변경 가능
}
