package com.example.PartTimeHR.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 애플리케이션 공통 설정값 (환경변수로 주입)
@Getter
@Component
public class AppProperties {

    // 인증 메일 링크의 기준 URL (백엔드)
    @Value("${app.base-url}")
    private String baseUrl;

    // 비밀번호 재설정 페이지의 기준 URL (프론트엔드)
    @Value("${app.frontend-url}")
    private String frontendUrl;
}
