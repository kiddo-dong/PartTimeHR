package com.example.PartTimeHR.employee.dto;

import lombok.Data;

@Data
public class EmployeeLoginRequest {
    private String loginId;
    private String password;
}
