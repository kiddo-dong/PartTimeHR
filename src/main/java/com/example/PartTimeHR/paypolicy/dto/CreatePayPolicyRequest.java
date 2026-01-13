package com.example.PartTimeHR.paypolicy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePayPolicyRequest {

    @NotBlank
    private String jobTitle;

    @NotNull
    private Integer hourlyWage;

    @NotNull
    private Boolean isDefault;
}
