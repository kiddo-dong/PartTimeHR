package com.example.PartTimeHR.paypolicy.domain;

import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayPolicyRepository extends JpaRepository<PayPolicy, Long> {

    List<PayPolicy> findByStoreId(Long storeId);

    Optional<PayPolicy> findByStoreIdAndIsDefaultTrue(Long storeId);

    // 매장 삭제 시 함께 정리
    void deleteAllByStoreId(Long storeId);

}
