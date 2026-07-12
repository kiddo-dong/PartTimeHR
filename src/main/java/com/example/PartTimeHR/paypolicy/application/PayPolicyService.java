package com.example.PartTimeHR.paypolicy.application;

import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.presentation.dto.CreatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.presentation.dto.UpdatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.application.PayPolicyMapper;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.domain.StoreRepository;
import com.example.PartTimeHR.store.application.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayPolicyService {

    private final PayPolicyRepository payPolicyRepository;
    private final StoreAccessService storeAccessService;
    private final PayPolicyMapper payPolicyMapper;

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

    @Transactional
    public void updatePayPolicy(Long storeId, Long payPolicyId, Long employerId, UpdatePayPolicyRequest request) {
        // 1. 가게 조회
        Store store = storeAccessService.findStore(storeId);

        // 2. 사장 소유 확인
        storeAccessService.getMyStore(storeId, employerId);

        // 3. 기존 정책 조회 (ID 기준)
        PayPolicy policy = payPolicyRepository.findById(payPolicyId)
                .orElseThrow(() -> new IllegalArgumentException("해당 급여 정책이 존재하지 않습니다."));

        // 4. MapStruct로 request 덮어쓰기
        payPolicyMapper.updatePayPolicyFromRequest(request, policy);

        // 5. JPA 트랜잭션 내에서 dirty checking으로 자동 저장
    }
}