package com.example.PartTimeHR.auth.service;

import com.example.PartTimeHR.auth.domain.AuthPrincipal;
import com.example.PartTimeHR.auth.dto.LoginRequest;
import com.example.PartTimeHR.auth.dto.LoginResponse;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final EmployerRepository employerRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request) {

        AuthPrincipal user = findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!user.isEmailVerified()) {
            throw new RuntimeException("이메일 인증이 필요합니다.");
        }

        String token = jwtProvider.createAccessToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        return new LoginResponse(token);
    }

    private AuthPrincipal findByEmail(String email) {

        return employerRepository.findByEmail(email)
                .<AuthPrincipal>map(e -> e)
                .orElseGet(() ->
                        employeeRepository.findByEmail(email)
                                .orElseThrow(() ->
                                        new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.")
                                )
                );
    }

}
