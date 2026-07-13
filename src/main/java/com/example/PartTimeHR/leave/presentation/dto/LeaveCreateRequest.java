package com.example.PartTimeHR.leave.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class LeaveCreateRequest {

    @NotNull
    private LocalDate leaveDate;
}
