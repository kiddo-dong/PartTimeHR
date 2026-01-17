package com.example.PartTimeHR.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
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
}
