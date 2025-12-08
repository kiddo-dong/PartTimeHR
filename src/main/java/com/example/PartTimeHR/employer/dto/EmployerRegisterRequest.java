package com.example.PartTimeHR.employer.dto;

import lombok.*;

@Data
public class EmployerRegisterRequest {
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private String storeName;
}