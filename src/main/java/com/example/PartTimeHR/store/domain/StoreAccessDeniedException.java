package com.example.PartTimeHR.store.domain;

// 내 가게가 아닌 경우 (권한 위반)
public class StoreAccessDeniedException extends RuntimeException {
    public StoreAccessDeniedException() {
        super("가게 접근 권한이 없습니다.");
    }
}
