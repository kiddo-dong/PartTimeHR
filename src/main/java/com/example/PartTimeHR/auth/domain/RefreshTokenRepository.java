package com.example.PartTimeHR.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    // 재로그인 시 기존 토큰 폐기 (계정당 refresh 토큰 1개)
    void deleteByAccountId(Long accountId);
}
