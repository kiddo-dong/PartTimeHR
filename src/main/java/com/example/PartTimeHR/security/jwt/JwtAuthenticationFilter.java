package com.example.PartTimeHR.security.jwt;

import com.example.PartTimeHR.auth.domain.AccountRepository;
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
    private final AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ CORS preflight 요청은 무조건 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        // ✅ 토큰이 있는 경우에만 인증 처리
        if (token != null && jwtProvider.validateToken(token)) {
            authenticate(token);
        }

        // 인증에 실패하면 SecurityContext를 비운 채 통과
        // → 보호된 URL은 authenticationEntryPoint가 401 JSON으로 응답
        filterChain.doFilter(request, response);
    }

    /**
     * 필터는 컨트롤러 밖이라 @RestControllerAdvice가 예외를 못 잡는다.
     * 따라서 여기서는 예외를 던지지 않고, 실패 시 인증을 세팅하지 않는 것으로 처리한다.
     * (토큰은 유효한데 계정이 삭제된 경우, role 클레임이 손상된 경우 등)
     */
    private void authenticate(String token) {
        try {
            Claims claims = jwtProvider.getClaims(token);
            String email = claims.getSubject();

            // Employer/Employee가 Account와 PK를 공유하므로, 로그인 시점엔
            // Account만 조회하면 충분하다 (role별로 다른 테이블을 뒤질 필요가 없다)
            CustomUserDetails userDetails = accountRepository.findByEmail(email)
                    .map(CustomUserDetails::new)
                    .orElse(null);

            if (userDetails == null) {
                return;
            }

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
