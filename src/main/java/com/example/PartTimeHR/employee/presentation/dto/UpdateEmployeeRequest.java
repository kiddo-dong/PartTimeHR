package com.example.PartTimeHR.employee.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Min(value = 1, message = "주휴일 요일은 1(월)~7(일)입니다.")
    @Max(value = 7, message = "주휴일 요일은 1(월)~7(일)입니다.")
    private Integer weeklyRestDay;  // 약정 주휴일 요일 변경
}
