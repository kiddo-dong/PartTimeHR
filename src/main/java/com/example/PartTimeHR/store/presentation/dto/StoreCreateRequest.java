package com.example.PartTimeHR.store.presentation.dto;

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
    private Integer weekStartDay;

    @NotNull
    private Boolean weeklyPayApplicable;
}
