package com.example.PartTimeHR.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeInfoResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String role;
    private Long employerId;
    private String employerName;
    private String storeName;
    private String jobTitle;
    private int hourlyWage;
    private LocalDateTime createdAt;
}

