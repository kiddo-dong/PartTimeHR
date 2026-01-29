package com.example.PartTimeHR.store.service;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.exception.EmployerNotFoundException;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.repository.PayPolicyRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.dto.StoreCreateRequest;
import com.example.PartTimeHR.store.dto.StoreInfoResponse;
import com.example.PartTimeHR.store.dto.StoreUpdateRequest;
import com.example.PartTimeHR.store.exception.StoreNotFoundException;
import com.example.PartTimeHR.store.mapper.StoreMapper;
import com.example.PartTimeHR.store.repository.StoreRepository;
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

        Store saved = storeRepository.save(store);

        return storeMapper.toInfoResponse(saved);
    }

    // ===== 수정 =====
    @Override
    @Transactional
    public StoreInfoResponse storeUpdateRequest(
            Long employerId,
            StoreUpdateRequest request
    ) {
        Store store = storeRepository.findByEmployerId(employerId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

        storeMapper.updateStoreFromRequest(request, store);

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

        // 가게 존재 여부 확인
        Store store = storeAccessService.findStore(storeId);

        // 내 가게인지 검증
        storeAccessService.getMyStore(storeId, employerId);

        //
        return storeMapper.toInfoResponse(store);
    }
}
