package com.example.PartTimeHR.invite.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreInviteCodeRepository extends JpaRepository<StoreInviteCode, Long> {

    Optional<StoreInviteCode> findByCode(String code);

    Optional<StoreInviteCode> findByStoreId(Long storeId);

    void deleteByStoreId(Long storeId);
}
