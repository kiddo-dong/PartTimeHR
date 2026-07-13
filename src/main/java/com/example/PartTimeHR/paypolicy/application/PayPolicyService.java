package com.example.PartTimeHR.paypolicy.application;

import com.example.PartTimeHR.employee.domain.EmployeeRepository;
import com.example.PartTimeHR.global.config.AppProperties;
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
    private final EmployeeRepository employeeRepository;
    private final StoreAccessService storeAccessService;
    private final PayPolicyMapper payPolicyMapper;
    private final AppProperties appProperties;

    /**
     * 최저임금(근로기준법·최저임금법) 미달 시급 차단.
     * "주휴 포함 시급" 계약 매장은 주휴수당을 별도 지급하지 않으므로
     * 시급이 최저임금 × 1.2(주휴 환산 근사치) 이상이어야 실질 최저임금을 지킨다.
     */
    private void validateMinimumWage(Integer hourlyWage, Store store) {
        if (hourlyWage == null) {
            return;
        }

        int minimum = appProperties.getMinimumWage();

        if (Boolean.TRUE.equals(store.getWeeklyAllowanceIncluded())) {
            int includedMinimum = (int) Math.ceil(minimum * 1.2);
            if (hourlyWage < includedMinimum) {
                throw new IllegalArgumentException(
                        "주휴 포함 시급 계약 매장은 시급이 최저임금의 1.2배("
                                + includedMinimum + "원) 이상이어야 합니다."
                );
            }
            return;
        }

        if (hourlyWage < minimum) {
            throw new IllegalArgumentException(
                    "시급은 최저임금(" + minimum + "원) 이상이어야 합니다."
            );
        }
    }

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

        validateMinimumWage(request.getHourlyWage(), store);

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

        validateMinimumWage(request.getHourlyWage(), store);

        // MapStruct는 setter 없는 엔티티에 빈 메서드를 생성해 no-op이었음 → 도메인 메서드 사용
        policy.update(request.getJobTitle(), request.getHourlyWage());

        // dirty checking으로 자동 저장
    }

    @Transactional
    public void deletePayPolicy(Long storeId, Long payPolicyId, Long employerId) {

        Store store = storeAccessService.getMyStore(storeId, employerId);

        PayPolicy policy = payPolicyRepository.findById(payPolicyId)
                .orElseThrow(PayPolicyNotFoundException::new);

        if (!policy.getStore().getId().equals(store.getId())) {
            throw new StoreAccessDeniedException();
        }

        // 기본 정책은 직원 등록의 fallback이므로 삭제 불가
        if (policy.isDefault()) {
            throw new IllegalStateException("기본 정책은 삭제할 수 없습니다.");
        }

        // 사용 중인 직원이 있으면 삭제 불가 (직원의 정책을 먼저 변경해야 함)
        if (employeeRepository.existsByPayPolicyId(policy.getId())) {
            throw new IllegalStateException("해당 정책을 사용 중인 직원이 있어 삭제할 수 없습니다.");
        }

        payPolicyRepository.delete(policy);
    }
}
