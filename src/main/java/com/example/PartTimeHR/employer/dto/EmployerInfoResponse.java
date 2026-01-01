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
public class EmployerInfoResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String storeName;
    private String role;
    private LocalDateTime createdAt;
    private Integer weekStartDay;
}

