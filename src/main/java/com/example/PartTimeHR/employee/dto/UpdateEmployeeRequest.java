package com.example.PartTimeHR.employee.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateEmployeeRequest {

    @Size(max = 30)
    private String name;  // 이름 (선택)

    @Size(max = 20)
    private String phone;  // 전화번호 (선택)

    @Size(min = 8, max = 20)
    private String password;  // 새 비밀번호 (선택)

    @Size(min = 8, max = 20)
    private String passwordConfirm;  // 비밀번호 확인 (선택)

    @Size(min = 8, max = 20)
    private String currentPassword;  // 현재 비밀번호 (비밀번호 변경 시 필수)
}

