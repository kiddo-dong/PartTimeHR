package com.example.PartTimeHR.paypolicy.repository;

import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.employer.domain.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayPolicyRepository extends JpaRepository<PayPolicy, Long> {

    // 사장님 기준 기본 정책
    Optional<PayPolicy> findByEmployerAndIsDefaultTrue(Employer employer);
    Optional<PayPolicy> findByEmployerAndIsDefaultTrueAndActiveTrue(Employer employer);
}