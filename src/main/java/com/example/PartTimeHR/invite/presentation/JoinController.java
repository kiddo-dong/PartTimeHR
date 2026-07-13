package com.example.PartTimeHR.invite.presentation;

import com.example.PartTimeHR.invite.application.InviteService;
import com.example.PartTimeHR.invite.presentation.dto.JoinRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 매장 초대코드로 직원이 스스로 가입 (인증 없이 호출 - 회원가입 자체이므로)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/join")
public class JoinController {

    private final InviteService inviteService;

    @PostMapping
    public ResponseEntity<Void> join(@Valid @RequestBody JoinRequest request) {
        inviteService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
