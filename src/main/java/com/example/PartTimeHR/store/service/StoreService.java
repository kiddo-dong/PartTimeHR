package com.example.PartTimeHR.store.service;
import com.example.PartTimeHR.store.dto.StoreCreateRequest;
import com.example.PartTimeHR.store.dto.StoreInfoResponse;

import java.util.List;

public interface StoreService {

    List<StoreInfoResponse> getMyStores(Long employerId);

    StoreInfoResponse getStore(Long storeId, Long employerId);

    StoreInfoResponse createStore(StoreCreateRequest request, Long employerId);
}