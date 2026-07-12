package com.example.PartTimeHR.common.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


// Server 배포 확인용 컨트롤러 (Auth 제거)
@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("PartTimeHR Server Running");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
