package com.example.PartTimeHR.employer.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployerSignupRequest {

    /* ===== Employer 정보 ===== */
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

    /* ===== 초기 findStore 정보 ===== */

    @NotBlank
    @Size(max = 50)
    private String storeName;

    @NotBlank
    @Size(max = 20)
    private String storePhone;

    @NotBlank
    @Size(max = 100)
    private String storeAddress;

    @NotNull
    @Min(value = 1, message = "주 시작 요일은 1(월)~7(일)입니다.")
    @Max(value = 7, message = "주 시작 요일은 1(월)~7(일)입니다.")
    private Integer weekStartDay;

    // 주휴수당이 시급에 포함된 계약인지 (생략 시 false = 별도 계산·지급)
    private Boolean weeklyAllowanceIncluded;

    // 상시 5인 이상 여부 (연장/야간 가산 적용) - 생략 시 false
    private Boolean fiveOrMoreEmployees;
}