package com.example.PartTimeHR.leave.presentation.dto;

import com.example.PartTimeHR.leave.domain.LeaveRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate leaveDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;

    public static LeaveResponse from(LeaveRequest leave) {
        return LeaveResponse.builder()
                .id(leave.getId())
                .employeeId(leave.getEmployee().getId())
                .employeeName(leave.getEmployee().getName())
                .leaveDate(leave.getLeaveDate())
                .status(leave.getStatus().name())
                .createdAt(leave.getCreatedAt())
                .decidedAt(leave.getDecidedAt())
                .build();
    }
}
