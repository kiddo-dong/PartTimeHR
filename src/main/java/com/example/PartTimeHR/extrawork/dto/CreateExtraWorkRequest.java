package com.example.PartTimeHR.extrawork.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateExtraWorkRequest {

    @NotNull
    private LocalDate workDate;

    private String reason;
}
