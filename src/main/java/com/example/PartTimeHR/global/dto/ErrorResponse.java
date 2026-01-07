package com.example.PartTimeHR.global.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final String code;
    private final String message;
    private final LocalDateTime timestamp;
}

