package com.example.PartTimeHR.global.handler;

import com.example.PartTimeHR.global.jwt.JwtProvider;
import com.example.PartTimeHR.global.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Slf4j
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    // private final Logger logger = Logger.getLogger(LoginSuccessHandler.class.getName());

    public LoginSuccessHandler(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getEmail();
        String role = userDetails.getRole().name(); // Enum -> String

        // SessionId Log
        // logger.info("Session_Id : " + request.getSession().getId());

        String accessToken = jwtProvider.createAccessToken(email, role);

        // FormLogin 후 jwt 발행 및 jwt인증 인가로 FormLogin Session 폐기
        request.getSession().invalidate();

        // jwt Log
        // logger.info("Access token: " + accessToken);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write("""
                {
                  "accessToken": "%s"
                }
                """.formatted(accessToken));
    }
}