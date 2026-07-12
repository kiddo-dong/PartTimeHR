package com.example.PartTimeHR.paypolicy.application;

import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.presentation.dto.PayPolicyResponse;
import com.example.PartTimeHR.paypolicy.presentation.dto.UpdatePayPolicyRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PayPolicyMapper {

    PayPolicyResponse toResponse(PayPolicy policy);

    List<PayPolicyResponse> toResponseList(List<PayPolicy> policies);

    void updatePayPolicyFromRequest(UpdatePayPolicyRequest request, @MappingTarget PayPolicy policy);
}
