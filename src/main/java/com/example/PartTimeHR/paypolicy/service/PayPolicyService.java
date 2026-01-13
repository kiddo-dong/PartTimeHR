package com.example.PartTimeHR.paypolicy.service;

import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.dto.CreatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.repository.PayPolicyRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.exception.StoreAccessDeniedException;
import com.example.PartTimeHR.store.exception.StoreNotFoundException;
import com.example.PartTimeHR.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayPolicyService {

    private final PayPolicyRepository payPolicyRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void createPayPolicy(Long storeId, Long employerId, CreatePayPolicyRequest request) {

        // 1️⃣ 가게 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(StoreNotFoundException::new);

        // 2️⃣ 사장 소유 확인
        if (!store.getEmployer().getId().equals(employerId)) {
            throw new StoreAccessDeniedException();
        }

        // 3️⃣ 기본 정책 처리
        if (request.getIsDefault()) {
            payPolicyRepository.findByStoreIdAndIsDefaultTrue(storeId)
                    .ifPresent(existing -> {
                        existing.setDefault(false);
                    });
        }

        // 4️⃣ 정책 생성
        PayPolicy policy = PayPolicy.builder()
                .store(store)
                .jobTitle(request.getJobTitle())
                .hourlyWage(request.getHourlyWage())
                .isDefault(request.getIsDefault())
                .active(true)
                .build();

        payPolicyRepository.save(policy);
    }
}
