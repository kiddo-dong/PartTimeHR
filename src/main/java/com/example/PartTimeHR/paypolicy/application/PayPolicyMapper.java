package com.example.PartTimeHR.paypolicy.application;

import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.presentation.dto.UpdatePayPolicyRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PayPolicyMapper {
    void updatePayPolicyFromRequest(UpdatePayPolicyRequest request, @MappingTarget PayPolicy policy);
}
