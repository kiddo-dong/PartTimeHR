package com.example.PartTimeHR.security.jwt;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.repository.EmployeeRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.employer.repository.EmployerRepository;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final EmployerRepository employerRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // FormLogin 방식 건너 뜀
        if (request.getRequestURI().equals("/api/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (token != null && jwtProvider.validateToken(token)) {

            Claims claims = jwtProvider.getClaims(token);
            Long id = claims.get("id", Long.class);
            String email = claims.getSubject();
            String roleStr = claims.get("role", String.class); // JWT에는 문자열로 저장됨
            Role role = Role.valueOf(roleStr); // 문자열 → Enum 변환

            CustomUserDetails userDetails; // UserDetails 구현체 CustomUserDetails 객체 사용

            switch (role) {

                case ROLE_EMPLOYER -> {
                    Employer employer = employerRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
                    userDetails = new CustomUserDetails(employer); // CustomUserDetails 객체 생성
                }

                case ROLE_EMPLOYEE -> {
                    Employee employee = employeeRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
                    userDetails = new CustomUserDetails(employee); // CustomUserDetails 객체 생성
                }
                default -> throw new RuntimeException("알 수 없는 사용자 역할");
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }



    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}

