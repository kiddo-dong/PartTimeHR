package com.example.PartTimeHR.auth.application;

import com.example.PartTimeHR.auth.domain.AuthPrincipal;
import com.example.PartTimeHR.auth.presentation.dto.LoginRequest;
import com.example.PartTimeHR.auth.presentation.dto.LoginResponse;
import com.example.PartTimeHR.auth.domain.InvalidCredentialsException;
import com.example.PartTimeHR.mail.domain.EmailNotVerifiedException;
import com.example.PartTimeHR.employee.domain.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.EmployerRepository;
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
            throw new InvalidCredentialsException();
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
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
                                .orElseThrow(InvalidCredentialsException::new)
                );
    }

}
