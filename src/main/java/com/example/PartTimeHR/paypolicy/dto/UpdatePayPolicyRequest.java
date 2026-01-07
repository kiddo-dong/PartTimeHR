package com.example.PartTimeHR.paypolicy.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePayPolicyRequest {
    private String jobTitle;     // 새 직급
    private Integer hourlyWage;      // 새 시급
}
