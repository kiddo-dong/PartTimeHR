package com.example.PartTimeHR.schedule.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Getter
public class CreateScheduleRequest {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotEmpty
    private Set<DayOfWeek> workingDays;
}
