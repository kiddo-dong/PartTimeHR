package com.example.PartTimeHR.paypolicy.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdatePayPolicyResponse {
    private Long employeeId;
    private String employeeName;
    private String jobTitle;
    private int hourlyWage;
}

