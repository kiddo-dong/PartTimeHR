package com.example.PartTimeHR.mail.application;

import com.example.PartTimeHR.mail.domain.MailCooldownException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 같은 이메일로의 반복 발송을 막는 단순 쿨다운 (인메모리).
 * 이메일 주소만 알면 인증/재설정 메일을 무한 발송시킬 수 있는 문제 방지용.
 * 서버 다중 인스턴스 환경에서는 Redis 등으로 교체 필요.
 */
@Component
public class MailCooldownGuard {

    private static final long COOLDOWN_MILLIS = 60_000L;

    private final Map<String, Long> lastSentAt = new ConcurrentHashMap<>();

    public void checkAndMark(String email) {
        long now = System.currentTimeMillis();
        Long last = lastSentAt.get(email);

        if (last != null && now - last < COOLDOWN_MILLIS) {
            throw new MailCooldownException();
        }

        lastSentAt.put(email, now);
    }
}
