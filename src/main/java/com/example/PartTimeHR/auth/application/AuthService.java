package com.example.PartTimeHR.auth.application;

import com.example.PartTimeHR.auth.domain.Account;
import com.example.PartTimeHR.auth.domain.AccountRepository;
import com.example.PartTimeHR.auth.domain.RefreshToken;
import com.example.PartTimeHR.auth.domain.RefreshTokenRepository;
import com.example.PartTimeHR.auth.presentation.dto.LoginRequest;
import com.example.PartTimeHR.auth.presentation.dto.LoginResponse;
import com.example.PartTimeHR.auth.domain.InvalidCredentialsException;
import com.example.PartTimeHR.mail.domain.EmailNotVerifiedException;
import com.example.PartTimeHR.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request) {

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (!account.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        String accessToken = createAccessToken(account);

        // 재로그인 시 기존 토큰 폐기 (계정당 refresh 토큰 1개)
        refreshTokenRepository.deleteByAccountId(account.getId());

        RefreshToken refreshToken = RefreshToken.create(account.getId());
        refreshTokenRepository.save(refreshToken);

        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    /**
     * refresh 토큰으로 access 토큰 재발급.
     * 사용자 존재/이메일 인증 상태를 재확인하므로, 탈퇴·미인증 전환된 계정은 갱신 불가.
     */
    public LoginResponse refresh(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(InvalidCredentialsException::new);

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidCredentialsException();
        }

        Account account = accountRepository.findById(refreshToken.getAccountId())
                .orElseThrow(InvalidCredentialsException::new);

        if (!account.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        return new LoginResponse(createAccessToken(account), refreshToken.getToken());
    }

    /**
     * 로그아웃 — refresh 토큰 폐기.
     * access 토큰은 블랙리스트가 없어 만료(24시간)까지는 유효하다.
     */
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.deleteByToken(refreshTokenValue);
    }

    private String createAccessToken(Account account) {
        return jwtProvider.createAccessToken(
                account.getEmail(),
                account.getId(),
                account.getRole().name()
        );
    }
}
