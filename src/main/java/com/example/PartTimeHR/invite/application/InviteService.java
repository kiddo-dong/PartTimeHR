package com.example.PartTimeHR.invite.application;

import com.example.PartTimeHR.auth.domain.User;
import com.example.PartTimeHR.auth.domain.UserRepository;
import com.example.PartTimeHR.employee.application.EmployeeAccessService;
import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.domain.EmployeeRepository;
import com.example.PartTimeHR.employee.domain.EmployeeStatus;
import com.example.PartTimeHR.employee.domain.PasswordMismatchException;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.invite.domain.StoreInviteCode;
import com.example.PartTimeHR.invite.domain.StoreInviteCodeRepository;
import com.example.PartTimeHR.invite.presentation.dto.InviteCodeResponse;
import com.example.PartTimeHR.invite.presentation.dto.JoinRequest;
import com.example.PartTimeHR.invite.presentation.dto.PendingEmployeeResponse;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyNotFoundException;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyRepository;
import com.example.PartTimeHR.store.application.StoreAccessService;
import com.example.PartTimeHR.store.domain.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

/**
 * 매장 초대코드로 직원이 스스로 가입하는 플로우.
 * 사장이 직접 등록하는 기존 경로(EmployeeService.createEmployee)와 별개로,
 * 직원이 코드+본인 정보로 가입하면 PENDING 상태로 생성되고 사장 승인 후 로그인이 열린다.
 */
@Service
@RequiredArgsConstructor
public class InviteService {

    // 혼동되기 쉬운 문자(0/O, 1/I/L) 제외 - 대면으로 코드를 불러주거나 옮겨 적을 때 실수 방지
    private static final String CODE_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StoreInviteCodeRepository storeInviteCodeRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PayPolicyRepository payPolicyRepository;
    private final StoreAccessService storeAccessService;
    private final EmployeeAccessService employeeAccessService;
    private final PasswordEncoder passwordEncoder;

    /* ===== 사장 ===== */

    // 초대 코드 조회 (없으면 새로 발급) - 매장당 하나, 만료 없음
    @Transactional
    public InviteCodeResponse getOrCreateCode(Long employerId, Long storeId) {
        Store store = storeAccessService.getMyStore(storeId, employerId);

        StoreInviteCode invite = storeInviteCodeRepository.findByStoreId(store.getId())
                .orElseGet(() -> storeInviteCodeRepository.save(
                        StoreInviteCode.create(store, generateUniqueCode())
                ));

        return new InviteCodeResponse(invite.getCode());
    }

    // 코드 재발급 (유출 등으로 기존 코드를 무효화하고 싶을 때)
    @Transactional
    public InviteCodeResponse regenerateCode(Long employerId, Long storeId) {
        Store store = storeAccessService.getMyStore(storeId, employerId);

        storeInviteCodeRepository.deleteByStoreId(store.getId());
        StoreInviteCode invite = storeInviteCodeRepository.save(
                StoreInviteCode.create(store, generateUniqueCode())
        );

        return new InviteCodeResponse(invite.getCode());
    }

    // 승인 대기 중인 직원 목록
    @Transactional(readOnly = true)
    public List<PendingEmployeeResponse> getPendingEmployees(Long employerId, Long storeId) {
        storeAccessService.getMyStore(storeId, employerId);

        return employeeRepository.findByStoreIdAndStatus(storeId, EmployeeStatus.PENDING).stream()
                .map(employee -> PendingEmployeeResponse.builder()
                        .employeeId(employee.getId())
                        .name(employee.getName())
                        .phone(employee.getPhone())
                        .email(employee.getUser().getEmail())
                        .requestedAt(employee.getCreatedAt())
                        .build())
                .toList();
    }

    // 승인 - 로그인 활성화 + 입사일 확정
    @Transactional
    public void approve(Long employerId, Long storeId, Long employeeId) {
        storeAccessService.getMyStore(storeId, employerId);

        Employee employee = findPendingOrThrow(employeeId, storeId);

        employee.approve(LocalDate.now());
        employee.getUser().activate();
    }

    // 거절 - 이메일이 영구히 묶이지 않도록 계정을 통째로 삭제 (User는 cascade로 함께 삭제)
    @Transactional
    public void reject(Long employerId, Long storeId, Long employeeId) {
        storeAccessService.getMyStore(storeId, employerId);

        Employee employee = findPendingOrThrow(employeeId, storeId);

        employeeRepository.delete(employee);
    }

    /* ===== 직원 (공개, 인증 없음) ===== */

    @Transactional
    public void join(JoinRequest request) {
        StoreInviteCode invite = storeInviteCodeRepository.findByCode(request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));

        Store store = invite.getStore();

        employeeAccessService.checkEmailDuplicates(request.getEmail());

        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new PasswordMismatchException();
        }

        PayPolicy defaultPolicy = payPolicyRepository.findByStoreIdAndIsDefaultTrue(store.getId())
                .orElseThrow(PayPolicyNotFoundException::new);

        // 사장 승인 전까지 로그인 불가 (emailVerified는 직원에게 의미 없는 게이트라 true, active만 false)
        User user = User.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_EMPLOYEE,
                true,
                false
        );
        userRepository.save(user);

        Employee employee = Employee.builder()
                .user(user)
                .name(request.getName())
                .phone(request.getPhone())
                .store(store)
                .payPolicy(defaultPolicy)
                .status(EmployeeStatus.PENDING)
                .build();

        employeeRepository.save(employee);
    }

    private Employee findPendingOrThrow(Long employeeId, Long storeId) {
        return employeeRepository.findByIdAndStoreIdAndStatus(employeeId, storeId, EmployeeStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("승인 대기 중인 해당 직원을 찾을 수 없습니다."));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = randomCode();
        } while (storeInviteCodeRepository.findByCode(code).isPresent());
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
