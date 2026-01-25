package com.example.PartTimeHR.auth.service;

import com.example.PartTimeHR.auth.dto.LoginRequest;
import com.example.PartTimeHR.auth.dto.LoginResponse;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request) {

        Employer employer = employerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다."));


        if (!passwordEncoder.matches(request.getPassword(), employer.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!employer.isEmailVerified()) {
            throw new RuntimeException("이메일 인증이 필요합니다.");
        }

        String token = jwtProvider.createAccessToken(
                employer.getEmail(),
                employer.getId(),
                employer.getRole().name()
        );

        return new LoginResponse(token);
    }
}
