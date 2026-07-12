package com.example.PartTimeHR.auth.presentation;

import com.example.PartTimeHR.auth.application.AuthService;
import com.example.PartTimeHR.auth.presentation.dto.LoginRequest;
import com.example.PartTimeHR.auth.presentation.dto.LoginResponse;
import com.example.PartTimeHR.auth.presentation.dto.TokenRefreshRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // access 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        LoginResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // 로그아웃 (refresh 토큰 폐기)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
