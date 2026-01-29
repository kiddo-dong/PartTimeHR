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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStoreFromRequest(StoreUpdateRequest request, @MappingTarget Store store);
}
