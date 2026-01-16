package com.example.PartTimeHR.store.mapper;

import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.dto.StoreInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    StoreInfoResponse toInfoResponse(Store store);

    List<StoreInfoResponse> toInfoResponseList(List<Store> stores);
}
