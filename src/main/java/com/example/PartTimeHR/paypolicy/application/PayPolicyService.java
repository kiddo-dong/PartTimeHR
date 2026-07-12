package com.example.PartTimeHR.paypolicy.application;

import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyNotFoundException;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyRepository;
import com.example.PartTimeHR.paypolicy.presentation.dto.CreatePayPolicyRequest;
import com.example.PartTimeHR.paypolicy.presentation.dto.PayPolicyResponse;
import com.example.PartTimeHR.paypolicy.presentation.dto.UpdatePayPolicyRequest;
import com.example.PartTimeHR.store.application.StoreAccessService;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.domain.StoreAccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayPolicyService {

    private final PayPolicyRepository payPolicyRepository;
    private final StoreAccessService storeAccessService;
    private final PayPolicyMapper payPolicyMapper;

    // 매장의 직급/시급 정책 목록 조회
    @Transactional(readOnly = true)
    public List<PayPolicyResponse> getPayPolicies(Long storeId, Long employerId) {
        storeAccessService.getMyStore(storeId, employerId);

        return payPolicyMapper.toResponseList(payPolicyRepository.findByStoreId(storeId));
    }

    @Transactional
    public void createPayPolicy(Long storeId, Long employerId, CreatePayPolicyRequest request) {

        // 내 매장인지 확인 (매장 조회까지 함께 처리)
        Store store = storeAccessService.getMyStore(storeId, employerId);

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

        Store store = storeAccessService.getMyStore(storeId, employerId);

        PayPolicy policy = payPolicyRepository.findById(payPolicyId)
                .orElseThrow(PayPolicyNotFoundException::new);

        // 다른 매장의 정책을 수정하지 못하도록 소속 검증
        if (!policy.getStore().getId().equals(store.getId())) {
            throw new StoreAccessDeniedException();
        }

        payPolicyMapper.updatePayPolicyFromRequest(request, policy);

        // dirty checking으로 자동 저장
    }
}
