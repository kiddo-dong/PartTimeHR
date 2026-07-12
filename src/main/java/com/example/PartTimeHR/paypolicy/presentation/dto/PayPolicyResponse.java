package com.example.PartTimeHR.paypolicy.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PayPolicyResponse {

    private Long id;
    private String jobTitle;
    private int hourlyWage;
    private boolean isDefault;
    private boolean active;
}

