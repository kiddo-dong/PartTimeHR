package com.example.PartTimeHR.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StoreCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String storePhone;

    @NotBlank
    private String address;

    @NotNull
    private Integer weekStartDay;

    @NotNull
    private Boolean weeklyPayApplicable;
}
