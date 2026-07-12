package com.example.PartTimeHR.store.application;

import com.example.PartTimeHR.employee.domain.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.EmployerNotFoundException;
import com.example.PartTimeHR.employer.domain.EmployerRepository;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.presentation.dto.StoreCreateRequest;
import com.example.PartTimeHR.store.presentation.dto.StoreInfoResponse;
import com.example.PartTimeHR.store.presentation.dto.StoreUpdateRequest;
import com.example.PartTimeHR.store.domain.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final EmployerRepository employerRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreMapper storeMapper;
    private final StoreAccessService storeAccessService;
    private final PayPolicyRepository payPolicyRepository;

    // 새 매장 생성
    @Transactional
    public StoreInfoResponse createStore(
            StoreCreateRequest request,
            Long employerId
    ) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(EmployerNotFoundException::new);

        Store store = Store.builder()
                .name(request.getStoreName())
                .phone(request.getStorePhone())
                .address(request.getStoreAddress())
                .weekStartDay(request.getWeekStartDay())
                .weeklyPayApplicable(request.getWeeklyPayApplicable())
                .employer(employer)
                .build();
        storeRepository.save(store);

        PayPolicy defaultPolicy = PayPolicy.builder()
                .store(store)
                .jobTitle("알바생")
                .hourlyWage(10320)
                .isDefault(true)
                .active(true)
                .build();
        payPolicyRepository.save(defaultPolicy);

        return storeMapper.toInfoResponse(store);
    }

    // ===== 수정 =====
    @Transactional
    public StoreInfoResponse updateStore(Long employerId, Long storeId, StoreUpdateRequest request) {
        // 내 매장인지 검증 + 조회
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 부분 수정 (null 필드는 기존 값 유지, dirty checking으로 자동 저장)
        store.update(
                request.getStoreName(),
                request.getStorePhone(),
                request.getStoreAddress(),
                request.getWeekStartDay(),
                request.getWeeklyPayApplicable()
        );

        return storeMapper.toInfoResponse(store);
    }

    // 매장 전체 조회 (매장이 없으면 빈 리스트가 정상 응답)
    @Transactional(readOnly = true)
    public List<StoreInfoResponse> getMyStores(Long employerId) {
        List<Store> stores = storeRepository.findAllByEmployerId(employerId);

        return storeMapper.toInfoResponseList(stores);
    }

    // 특정 매장 조회
    @Transactional(readOnly = true)
    public StoreInfoResponse getStore(Long storeId, Long employerId) {

        // 내 가게인지 검증 (존재 확인까지 함께 처리)
        Store store = storeAccessService.getMyStore(storeId, employerId);

        return storeMapper.toInfoResponse(store);
    }

    // ===== 삭제 =====
    @Transactional
    public void deleteStore(Long employerId, Long storeId) {

        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 직원(과 그에 딸린 스케줄/근무기록)이 남아 있으면 삭제 불가
        if (employeeRepository.existsByStoreId(store.getId())) {
            throw new IllegalStateException("소속 직원이 있는 매장은 삭제할 수 없습니다. 직원을 먼저 삭제해주세요.");
        }

        // 시급 정책 정리 후 매장 삭제
        payPolicyRepository.deleteAllByStoreId(store.getId());
        storeRepository.delete(store);
    }
}
