package com.example.PartTimeHR.store.service;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.exception.EmployerNotFoundException;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.dto.StoreCreateRequest;
import com.example.PartTimeHR.store.dto.StoreInfoResponse;
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

        Store saved = storeRepository.save(store);

        return storeMapper.toInfoResponse(saved);
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
