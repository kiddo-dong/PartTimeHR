package com.example.PartTimeHR.employer.presentation.dto;

import com.example.PartTimeHR.store.domain.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EmployerInfoResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private LocalDateTime createdAt;
}