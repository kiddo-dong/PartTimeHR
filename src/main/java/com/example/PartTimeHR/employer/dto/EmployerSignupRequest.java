package com.example.PartTimeHR.employer.dto;

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
    private Integer weekStartDay;

    @NotNull
    private Boolean weeklyPayApplicable;
}