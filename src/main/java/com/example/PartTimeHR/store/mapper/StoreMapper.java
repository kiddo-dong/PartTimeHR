package com.example.PartTimeHR.store.mapper;

import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.dto.StoreInfoResponse;
import com.example.PartTimeHR.store.dto.StoreUpdateRequest;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    @Mapping(source = "name", target = "storeName")
    @Mapping(source = "phone", target = "storePhone")
    @Mapping(source = "address", target = "storeAddress")
    StoreInfoResponse toInfoResponse(Store store);

    List<StoreInfoResponse> toInfoResponseList(List<Store> stores);

    // 수정: null값 무시할 때 setter 제대로 호출하도록
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "name", source = "storeName")
    @Mapping(target = "phone", source = "storePhone")
    @Mapping(target = "address", source = "storeAddress")
    @Mapping(target = "weekStartDay", source = "weekStartDay")
    @Mapping(target = "weeklyPayApplicable", source = "weeklyPayApplicable")
    void updateStoreFromRequest(StoreUpdateRequest request, @MappingTarget Store store);
}
