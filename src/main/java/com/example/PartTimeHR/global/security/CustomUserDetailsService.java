package com.example.PartTimeHR.global.security;

import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
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
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        return employerRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseGet(() ->
                        employeeRepository.findByEmail(email)
                                .map(CustomUserDetails::new)
                                .orElseThrow(() ->
                                        new UsernameNotFoundException("사용자를 찾을 수 없습니다.")
                                )
                );
    }
}
