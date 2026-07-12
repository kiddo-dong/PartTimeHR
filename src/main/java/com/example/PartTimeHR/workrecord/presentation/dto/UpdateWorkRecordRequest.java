package com.example.PartTimeHR.workrecord.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UpdateWorkRecordRequest {

    private LocalDateTime clockInTime;     // 선택

    private LocalDateTime breakStartTime;  // 선택

    private LocalDateTime breakEndTime;    // 선택

    private LocalDateTime clockOutTime;    // 선택

    private String memo;                   // 선택
}

