package com.example.PartTimeHR.store.service;
import com.example.PartTimeHR.store.dto.StoreCreateRequest;
import com.example.PartTimeHR.store.dto.StoreInfoResponse;
import com.example.PartTimeHR.store.dto.StoreUpdateRequest;

import java.util.List;

public interface StoreService {
    // === 생성 ===
    List<StoreInfoResponse> getMyStores(Long employerId);

    // === 수정 ===
    StoreInfoResponse storeUpdateRequest(Long employerId, StoreUpdateRequest storeUpdateRequest);

    // === 조회 ===
    StoreInfoResponse getStore(Long storeId, Long employerId);

    StoreInfoResponse createStore(StoreCreateRequest request, Long employerId);
}