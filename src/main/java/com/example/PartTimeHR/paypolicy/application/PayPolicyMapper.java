package com.example.PartTimeHR.paypolicy.application;

import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.presentation.dto.PayPolicyResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PayPolicyMapper {

    PayPolicyResponse toResponse(PayPolicy policy);

    List<PayPolicyResponse> toResponseList(List<PayPolicy> policies);
}
