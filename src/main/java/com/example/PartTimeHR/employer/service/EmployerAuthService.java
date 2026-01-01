package com.example.PartTimeHR.employer.service;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.employer.dto.EmployerSignupRequest;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployerAuthService {
    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;

    // signup logic
    @Transactional
    public void signup(EmployerSignupRequest request) {

        // 이메일 중복 검사
        if (employerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 확인 검사
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // Employer 생성
        Employer employer = Employer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .storeName(request.getStoreName())
                .weekStartDay(1)  // 기본값: 월요일
                .role(Role.ROLE_EMPLOYER)
                .build();

        // 저장
        employerRepository.save(employer);
    }
}
