package com.example.PartTimeHR.employer.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateEmployerRequest {

    @Size(max = 30)
    private String name;  // 이름 (선택)

    @Size(max = 20)
    private String phone;  // 전화번호 (선택)

    @Size(max = 50)
    private String storeName;  // 가게 이름 (선택)

    @Size(min = 8, max = 20)
    private String password;  // 새 비밀번호 (선택)

    @Size(min = 8, max = 20)
    private String passwordConfirm;  // 비밀번호 확인 (선택)

    @Size(min = 8, max = 20)
    private String currentPassword;  // 현재 비밀번호 (비밀번호 변경 시 필수)

    private Integer weekStartDay;  // 주간 시작 요일 (1=월요일, 2=화요일, ..., 7=일요일)
}

