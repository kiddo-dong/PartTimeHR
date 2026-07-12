package com.example.PartTimeHR.security.customuser;

import com.example.PartTimeHR.mail.domain.EmailNotVerifiedException;
import com.example.PartTimeHR.employee.domain.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployerRepository employerRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        // Employer 먼저 조회
        var employerOpt = employerRepository.findByEmail(email);

        if (employerOpt.isPresent()) {
            var employer = employerOpt.get();

            // 🔥 여기
            if (!employer.isEmailVerified()) {
                throw new EmailNotVerifiedException();
            }

            return new CustomUserDetails(employer);
        }

        // Employee 조회 (직원은 이메일 인증 안함)
        return employeeRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() ->
                        new UsernameNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다.")
                );
    }
}
