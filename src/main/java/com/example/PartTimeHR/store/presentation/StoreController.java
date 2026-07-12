package com.example.PartTimeHR.store.presentation;

import com.example.PartTimeHR.paypolicy.presentation.dto.PayPolicyResponse;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyRepository;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.presentation.dto.StoreCreateRequest;
import com.example.PartTimeHR.store.presentation.dto.StoreInfoResponse;
import com.example.PartTimeHR.store.presentation.dto.StoreUpdateRequest;
import com.example.PartTimeHR.store.domain.StoreAccessDeniedException;
import com.example.PartTimeHR.store.domain.StoreNotFoundException;
import com.example.PartTimeHR.store.domain.StoreRepository;
import com.example.PartTimeHR.store.application.StoreService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/stores")
@PreAuthorize("hasRole('EMPLOYER')")
public class StoreController {

    private final StoreService storeService;
    private final StoreRepository storeRepository;
    private final PayPolicyRepository payPolicyRepository;


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

    // ===== 가게의 PayPolicy(직급/시급) 목록 조회
    @GetMapping("/{storeId}/paypolicies")
    public ResponseEntity<List<PayPolicyResponse>> getPayPolicies(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long employerId = userDetails.getId();

        Store store = storeRepository.findById(storeId)
                .orElseThrow(StoreNotFoundException::new);

        if (!store.getEmployer().getId().equals(employerId)) {
            throw new StoreAccessDeniedException();
        }

        List<PayPolicyResponse> policies = payPolicyRepository.findByStoreId(storeId).stream()
                .map(p -> new PayPolicyResponse(
                        p.getId(),
                        p.getJobTitle(),
                        p.getHourlyWage(),
                        p.isDefault(),
                        p.isActive()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(policies);
    }
}