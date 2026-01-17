package com.example.PartTimeHR.paypolicy.service;

import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.dto.CreatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.repository.PayPolicyRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.repository.StoreRepository;
import com.example.PartTimeHR.store.service.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayPolicyService {

    private final PayPolicyRepository payPolicyRepository;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;

    @Transactional
    public void createPayPolicy(Long storeId, Long employerId, CreatePayPolicyRequest request) {

        // 가게 조회
        Store store = storeAccessService.findStore(storeId);

        // 사장 소유 확인
        storeAccessService.getMyStore(storeId, employerId);

        // 정책 생성
        PayPolicy policy = PayPolicy.builder()
                .store(store)
                .jobTitle(request.getJobTitle())
                .hourlyWage(request.getHourlyWage())
                .isDefault(false)
                .active(true)
                .build();

        payPolicyRepository.save(policy);
    }
}