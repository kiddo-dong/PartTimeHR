package com.example.PartTimeHR.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StoreInfoResponse {
    private Long id;
    private String storeName;
    private String storePhone;
    private String storeAddress;
    private Integer weekStartDay;
    private Boolean weeklyPayApplicable;
    private LocalDateTime createdAt;
}
