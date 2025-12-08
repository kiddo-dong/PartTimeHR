package com.example.PartTimeHR.employee.dto;

import lombok.Data;

@Data
public class EmployeeRegisterRequest {
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private Long employerId; // 어떤 사장에게 귀속될지
}

