package com.example.PartTimeHR.employer.dto;

import lombok.Data;

@Data
public class EmployerLoginRequest {
    private String loginId;
    private String password;
}