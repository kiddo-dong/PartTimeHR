package com.example.PartTimeHR.store.domain;


public class StoreNotFoundException extends RuntimeException {

    public StoreNotFoundException() {
        super("가게를 찾을 수 없습니다.");
    }
}
