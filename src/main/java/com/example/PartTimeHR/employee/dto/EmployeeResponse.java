package com.example.PartTimeHR.employee.dto;

import lombok.Data;

@Data
public class EmployeeResponse {
    private String loginId;
    private String name;
    private String phone;
    private Long employerId;
    private String employerName;
    private String storeName;
}
