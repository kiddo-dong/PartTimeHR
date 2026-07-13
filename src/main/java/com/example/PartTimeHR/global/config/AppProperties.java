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

    // 최저임금 (원/시간)
    @Value("${payroll.minimum-wage}")
    private int minimumWage;

    /* 4대보험 근로자 부담 요율 (매년 갱신) */

    @Value("${insurance.national-pension-rate}")
    private double nationalPensionRate;

    @Value("${insurance.health-rate}")
    private double healthRate;

    // 건강보험료에 곱하는 요율
    @Value("${insurance.long-term-care-rate}")
    private double longTermCareRate;

    @Value("${insurance.employment-rate}")
    private double employmentRate;
}
