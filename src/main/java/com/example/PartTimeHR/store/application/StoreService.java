package com.example.PartTimeHR.store.application;
import com.example.PartTimeHR.store.presentation.dto.StoreCreateRequest;
import com.example.PartTimeHR.store.presentation.dto.StoreInfoResponse;
import com.example.PartTimeHR.store.presentation.dto.StoreUpdateRequest;

import java.util.List;

public interface StoreService {
    // === 생성 ===
    List<StoreInfoResponse> getMyStores(Long employerId);

    // === 수정 ===
    StoreInfoResponse updateStore(Long employerId, Long storeId ,StoreUpdateRequest storeUpdateRequest);

    // === 조회 ===
    StoreInfoResponse getStore(Long storeId, Long employerId);

    StoreInfoResponse createStore(StoreCreateRequest request, Long employerId);
}