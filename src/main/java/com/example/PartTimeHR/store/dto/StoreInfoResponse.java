package com.example.PartTimeHR.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StoreInfoResponse {
    private Long id;
    private String name;
    private String storePhone;
    private String address;
    private Integer weekStartDay;
    private Boolean weeklyPayApplicable;
    private LocalDateTime createdAt;
}
