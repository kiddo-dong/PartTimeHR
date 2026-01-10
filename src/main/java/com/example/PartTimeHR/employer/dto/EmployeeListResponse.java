package com.example.PartTimeHR.employer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeListResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String jobTitle;
    private Integer hourlyWage;
    private LocalDateTime createdAt;
}

