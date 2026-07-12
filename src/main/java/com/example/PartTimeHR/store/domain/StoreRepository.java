package com.example.PartTimeHR.store.domain;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    // 내 가게 전체 조회
    List<Store> findAllByEmployer(Employer employer);

    List<Store> findAllByEmployerId(Long employerId);

    Optional<Store> findByIdAndEmployerId(Long id, Long employerId);
}