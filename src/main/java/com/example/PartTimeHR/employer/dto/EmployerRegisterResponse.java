package com.example.PartTimeHR.employer.dto;

import lombok.Data;

@Data
public class EmployerRegisterResponse {
    private String name;
    private String storeName;
    private String message;

    public EmployerRegisterResponse( String name, String storeName, String message) {
        this.name = name;
        this.storeName = storeName;
        this.message = message;
    }
}
