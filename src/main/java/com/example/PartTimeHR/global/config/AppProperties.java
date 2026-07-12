package com.example.PartTimeHR.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 애플리케이션 공통 설정값 (환경변수로 주입)
@Getter
@Component
public class AppProperties {

    // 인증/재설정 메일 링크의 기준 URL
    @Value("${app.base-url}")
    private String baseUrl;
}
