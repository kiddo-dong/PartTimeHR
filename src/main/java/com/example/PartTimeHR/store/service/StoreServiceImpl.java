package com.example.PartTimeHR.store.service;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.exception.EmployerNotFoundException;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.dto.StoreCreateRequest;
import com.example.PartTimeHR.store.dto.StoreInfoResponse;
import com.example.PartTimeHR.store.exception.StoreAccessDeniedException;
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

    @Transactional
    @Override
    public StoreInfoResponse createStore(
            StoreCreateRequest request,
            Long employerId
    ) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(EmployerNotFoundException::new);

        Store store = Store.create(
                request.getName(),
                request.getStorePhone(),
                request.getAddress(),
                request.getWeekStartDay(),
                request.getWeeklyPayApplicable(),
                employer
        );

        Store saved = storeRepository.save(store);

        return storeMapper.toInfoResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<StoreInfoResponse> getMyStores(Long employerId) {
        List<Store> stores = storeRepository.findAllByEmployerId(employerId);

        if (stores.isEmpty()) {
            throw new StoreNotFoundException();
        }

        return storeMapper.toInfoResponseList(stores);
    }

    @Transactional(readOnly = true)
    @Override
    public StoreInfoResponse getStore(Long storeId, Long employerId) {

        // 가게 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(StoreNotFoundException::new);

        // 내 가게인지 검증
        if (!store.getEmployer().getId().equals(employerId)) {
            log.warn(
                    "Store access denied. storeId={}, employerId={}",
                    storeId, employerId
            );
            throw new StoreAccessDeniedException();
        }

        //
        return storeMapper.toInfoResponse(store);
    }
}
