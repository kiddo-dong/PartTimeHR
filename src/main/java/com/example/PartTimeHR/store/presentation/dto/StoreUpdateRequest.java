package com.example.PartTimeHR.store.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreUpdateRequest {

    private String storeName;

    private String storePhone;

    private String storeAddress;

    @Min(value = 1, message = "주 시작 요일은 1(월)~7(일)입니다.")
    @Max(value = 7, message = "주 시작 요일은 1(월)~7(일)입니다.")
    private Integer weekStartDay;

    private Boolean weeklyAllowanceIncluded;

    private Boolean fiveOrMoreEmployees;
}