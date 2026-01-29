package com.example.PartTimeHR.store.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreUpdateRequest {

    private String storeName;

    private String storePhone;

    private String storeAddress;

    private Integer weekStartDay;

    private Boolean weeklyPayApplicable;
}