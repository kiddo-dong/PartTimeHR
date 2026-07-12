package com.example.PartTimeHR.workrecord.domain;

public class WorkRecordNotFoundException extends RuntimeException {
    public WorkRecordNotFoundException(String message) {
        super(message);
    }
}

