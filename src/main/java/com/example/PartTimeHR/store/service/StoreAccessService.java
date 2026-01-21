package com.example.PartTimeHR.store.service;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.exception.StoreAccessDeniedException;
import com.example.PartTimeHR.store.exception.StoreNotFoundException;
import com.example.PartTimeHR.store.repository.StoreRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreAccessService {

    private final StoreRepository storeRepository;

    // 존재하는 매장인지 검증
    public Store findStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(StoreNotFoundException::new);

    }

    // 접근 가능한 매장인지 검증
    public Store getMyStore(Long storeId, Long employerId) {
        return storeRepository
                .findByIdAndEmployerId(storeId, employerId)
                .orElseThrow(StoreAccessDeniedException::new);
    }

    public void validateEmployeeInStore(Store store, Employee employee) {
        if (!employee.getStore().getId().equals(store.getId())) {
            throw new IllegalStateException("해당 매장의 직원이 아닙니다.");
        }
    }
}