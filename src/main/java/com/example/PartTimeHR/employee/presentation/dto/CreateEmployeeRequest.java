package com.example.PartTimeHR.employee.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateEmployeeRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @NotBlank
    @Size(min = 8, max = 20)
    private String passwordConfirm;

    @NotBlank
    @Size(max = 30)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String phone;

    private Long payPolicyId; // 선택적, null이면 기본 정책 사용

    @Min(value = 1, message = "주휴일 요일은 1(월)~7(일)입니다.")
    @Max(value = 7, message = "주휴일 요일은 1(월)~7(일)입니다.")
    private Integer weeklyRestDay; // 약정 주휴일 요일 (선택)

    private LocalDate hiredAt; // 입사일 (생략 시 등록일)
}
