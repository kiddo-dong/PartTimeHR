package com.example.PartTimeHR.store.application;

import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.presentation.dto.StoreInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    @Mapping(source = "name", target = "storeName")
    @Mapping(source = "phone", target = "storePhone")
    @Mapping(source = "address", target = "storeAddress")
    StoreInfoResponse toInfoResponse(Store store);

    List<StoreInfoResponse> toInfoResponseList(List<Store> stores);
}
