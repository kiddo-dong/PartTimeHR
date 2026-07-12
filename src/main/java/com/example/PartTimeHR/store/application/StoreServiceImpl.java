package com.example.PartTimeHR.store.application;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.EmployerNotFoundException;
import com.example.PartTimeHR.employer.domain.EmployerRepository;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.presentation.dto.StoreCreateRequest;
import com.example.PartTimeHR.store.presentation.dto.StoreInfoResponse;
import com.example.PartTimeHR.store.presentation.dto.StoreUpdateRequest;
import com.example.PartTimeHR.store.domain.StoreNotFoundException;
import com.example.PartTimeHR.store.application.StoreMapper;
import com.example.PartTimeHR.store.domain.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final EmployerRepository employerRepository;
    private final StoreMapper storeMapper;
    private final StoreAccessService storeAccessService;
    private final PayPolicyRepository payPolicyRepository;

    // 새 매장 생성
    @Transactional
    @Override
    public StoreInfoResponse createStore(
            StoreCreateRequest request,
            Long employerId
    ) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(EmployerNotFoundException::new);

        Store store = Store.create(
                request.getStoreName(),
                request.getStorePhone(),
                request.getStoreAddress(),
                request.getWeekStartDay(),
                request.getWeeklyPayApplicable(),
                employer
        );
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
    @Override
    @Transactional
    public StoreInfoResponse updateStore(Long employerId, Long storeId, StoreUpdateRequest request) {
        // storeId로 정확히 조회, 동시에 owner(employerId) 확인
        Store store = storeRepository.findByIdAndEmployerId(storeId, employerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장을 찾을 수 없습니다."));

        // DTO 값으로 엔티티 업데이트
        storeMapper.updateStoreFromRequest(request, store);

        // 업데이트 후 DTO 반환
        return storeMapper.toInfoResponse(store);
    }


    // 매장 전체 조회
    @Transactional(readOnly = true)
    @Override
    public List<StoreInfoResponse> getMyStores(Long employerId) {
        List<Store> stores = storeRepository.findAllByEmployerId(employerId);

        if (stores.isEmpty()) {
            throw new StoreNotFoundException();
        }

        return storeMapper.toInfoResponseList(stores);
    }

    // 특정 매장 조회
    @Transactional(readOnly = true)
    @Override
    public StoreInfoResponse getStore(Long storeId, Long employerId) {

        // 내 가게인지 검증 (존재 확인까지 함께 처리)
        Store store = storeAccessService.getMyStore(storeId, employerId);

        return storeMapper.toInfoResponse(store);
    }
}
