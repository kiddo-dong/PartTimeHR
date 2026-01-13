package com.example.PartTimeHR.store.repository;

import com.example.PartTimeHR.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findAllByEmployerId(Long employerId);

    Optional<Store> findByIdAndEmployerId(Long id, Long employerId);
}
