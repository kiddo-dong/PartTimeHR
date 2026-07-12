package com.example.PartTimeHR.store.presentation;

import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import com.example.PartTimeHR.store.presentation.dto.StoreCreateRequest;
import com.example.PartTimeHR.store.presentation.dto.StoreInfoResponse;
import com.example.PartTimeHR.store.presentation.dto.StoreUpdateRequest;
import com.example.PartTimeHR.store.application.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
@PreAuthorize("hasRole('EMPLOYER')")
public class StoreController {

    private final StoreService storeService;

    // ===== 생성 =====
    @PostMapping
    public ResponseEntity<StoreInfoResponse> createStore(
            @Valid @RequestBody StoreCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long employerId = userDetails.getId();

        StoreInfoResponse response =
                storeService.createStore(request, employerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ===== 수정 =====
    @PutMapping("/{storeId}")
    public ResponseEntity<StoreInfoResponse> updateStore(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // storeId와 로그인한 employerId를 함께 전달
        StoreInfoResponse response = storeService.updateStore(userDetails.getId(), storeId, request);
        return ResponseEntity.ok(response);
    }

    // ===== 조회 =====
    // 전체 조회
    @GetMapping
    public ResponseEntity<List<StoreInfoResponse>> myStores(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<StoreInfoResponse> responses = storeService.getMyStores(userDetails.getId());

        return ResponseEntity.ok(responses);
    }

    // 특정 매장 조회
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreInfoResponse> storeDetail(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        StoreInfoResponse response = storeService.getStore(storeId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    // ===== 삭제 (소속 직원이 있으면 409) =====
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        storeService.deleteStore(userDetails.getId(), storeId);
        return ResponseEntity.noContent().build();
    }
}