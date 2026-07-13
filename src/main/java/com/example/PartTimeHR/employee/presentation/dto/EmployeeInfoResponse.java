package com.example.PartTimeHR.employee.presentation.dto;

import com.example.PartTimeHR.employee.domain.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

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
    private Integer weeklyRestDay;
    private LocalDate hiredAt;
    private EmployeeStatus status;
}
