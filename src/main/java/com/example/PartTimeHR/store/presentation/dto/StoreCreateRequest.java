package com.example.PartTimeHR.store.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StoreCreateRequest {

    @NotBlank
    private String storeName;

    @NotBlank
    private String storePhone;

    @NotBlank
    private String storeAddress;

    @NotNull
    @Min(value = 1, message = "주 시작 요일은 1(월)~7(일)입니다.")
    @Max(value = 7, message = "주 시작 요일은 1(월)~7(일)입니다.")
    private Integer weekStartDay;

    @NotNull
    private Boolean weeklyPayApplicable;

    // 상시 5인 이상 여부 (연장/야간 가산 적용) - 생략 시 false
    private Boolean fiveOrMoreEmployees;
}
