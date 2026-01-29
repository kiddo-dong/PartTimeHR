package com.example.PartTimeHR.paypolicy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdatePayPolicyRequest {
    @NotBlank
    private String jobTitle;

    @NotNull
    private Integer hourlyWage;
}
