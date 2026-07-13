package com.example.PartTimeHR.invite.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PendingEmployeeResponse {
    private Long employeeId;
    private String name;
    private String phone;
    private String email;
    private LocalDateTime requestedAt;
}
