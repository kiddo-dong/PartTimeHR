package com.example.PartTimeHR.employee.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmployeeInfoResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private Long storeId;
    private String storeName;
    private String jobTitle;
    private int hourlyWage;
}
