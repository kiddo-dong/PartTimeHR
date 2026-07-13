package com.example.PartTimeHR.employee.application;

import com.example.PartTimeHR.auth.domain.User;
import com.example.PartTimeHR.auth.domain.UserRepository;
import com.example.PartTimeHR.auth.domain.RefreshTokenRepository;
import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.presentation.dto.CreateEmployeeRequest;
import com.example.PartTimeHR.employee.presentation.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.presentation.dto.UpdateEmployeeRequest;
import com.example.PartTimeHR.employee.domain.PasswordMismatchException;
import com.example.PartTimeHR.employee.application.EmployeeMapper;
import com.example.PartTimeHR.employee.domain.EmployeeRepository;
import com.example.PartTimeHR.paypolicy.domain.PayPolicy;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyNotFoundException;
import com.example.PartTimeHR.paypolicy.domain.PayPolicyRepository;
import com.example.PartTimeHR.schedule.domain.ScheduleRepository;
import com.example.PartTimeHR.workrecord.domain.WorkRecordRepository;
import com.example.PartTimeHR.store.domain.Store;
import com.example.PartTimeHR.store.domain.StoreAccessDeniedException;
import com.example.PartTimeHR.employer.domain.Role;
import com.example.PartTimeHR.store.application.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PayPolicyRepository payPolicyRepository;
    private final ScheduleRepository scheduleRepository;
    private final WorkRecordRepository workRecordRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final StoreAccessService storeAccessService;
    private final EmployeeAccessService employeeAccessService;

    // 직원 생성
    @Transactional
    public EmployeeInfoResponse createEmployee(Long storeId, CreateEmployeeRequest request, Long employerId) {

        // 내 매장인지 확인 (매장 조회까지 함께 처리)
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 이메일 중복 확인 (직원 전체 + 사장 계정, 로그인이 이메일 하나로 이뤄지므로 전역 검사)
        employeeAccessService.checkEmailDuplicates(request.getEmail());

        // 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new PasswordMismatchException();
        }

        // PayPolicy 결정
        PayPolicy policy;
        if (request.getPayPolicyId() != null) {
            policy = getStorePolicy(request.getPayPolicyId(), store);
        } else {
            policy = payPolicyRepository.findByStoreIdAndIsDefaultTrue(store.getId())
                    .orElseThrow(PayPolicyNotFoundException::new);
        }

        // User 생성 - Employee보다 먼저 저장해야 PK가 생성돼 공유할 수 있다
        // 직원은 사장이 등록해주므로 이메일 인증 절차가 없다 (emailVerified=true)
        User user = User.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_EMPLOYEE,
                true
        );
        userRepository.save(user);

        // Employee 생성 (User와 PK 공유)
        Employee employee = Employee.builder()
                .user(user)
                .name(request.getName())
                .phone(request.getPhone())
                .store(store)
                .payPolicy(policy)
                .weeklyRestDay(request.getWeeklyRestDay())
                .hiredAt(request.getHiredAt() != null ? request.getHiredAt() : LocalDate.now())
                .build();

        Employee saved = employeeRepository.save(employee);

        return employeeMapper.toInfoResponse(saved);
    }

    @Transactional
    public EmployeeInfoResponse updateEmployee(
            Long employerId,
            Long storeId,
            Long employeeId,
            UpdateEmployeeRequest request
    ) {
        // 매장 소유 검증
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 직원 조회 + 해당 매장 소속 검증 (영속 상태)
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

        /* ===== 비밀번호 변경 로직 ===== */
        if (request.getPassword() != null || request.getPasswordConfirm() != null) {

            // 둘 중 하나만 들어온 경우
            if (request.getPassword() == null || request.getPasswordConfirm() == null) {
                throw new IllegalArgumentException("비밀번호와 비밀번호 확인을 모두 입력해야 합니다.");
            }

            // 불일치
            if (!request.getPassword().equals(request.getPasswordConfirm())) {
                throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
            }

            employee.getUser().changePassword(
                    passwordEncoder.encode(request.getPassword())
            );

            // 비밀번호가 바뀌면 기존 세션(refresh 토큰) 폐기
            refreshTokenRepository.deleteByUserId(employee.getId());
        }

        /* ===== 기본 정보 변경 ===== */
        if (request.getName() != null || request.getPhone() != null) {
            employee.updateBasicInfo(
                    request.getName(),
                    request.getPhone()
            );
        }

        /* ===== 주휴일 요일 변경 ===== */
        if (request.getWeeklyRestDay() != null) {
            employee.assignWeeklyRestDay(request.getWeeklyRestDay());
        }

        /* ===== 페이 정책 변경 ===== */
        if (request.getPayPolicyId() != null) {
            employee.changePayPolicy(
                    getStorePolicy(request.getPayPolicyId(), store)
            );
        }

        // save() 필요 없음 (Dirty Checking)
        return employeeMapper.toInfoResponse(employee);
    }

    // 직원 삭제
    // 주의(MVP): 스케줄·근무 기록도 함께 삭제되어 급여 이력이 사라진다.
    // 이력 보존이 필요해지면 soft delete로 전환할 것.
    @Transactional
    public void deleteEmployee(Long employerId, Long storeId, Long employeeId) {

        // 매장 소유 검증
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 직원 조회 + 해당 매장 소속 검증
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

        // FK 참조 데이터 정리 후 삭제
        scheduleRepository.deleteAllByEmployee(employee);
        workRecordRepository.deleteAllByEmployee(employee);

        // refresh 토큰 폐기 - 안 지우면 같은 이메일로 새 직원 등록 시
        // 삭제된 직원의 토큰으로 새 계정의 access 토큰을 발급받을 수 있다
        refreshTokenRepository.deleteByUserId(employee.getId());

        // User는 cascade(REMOVE)로 함께 삭제된다
        employeeRepository.delete(employee);
    }

    // 정책 조회 + 해당 매장 소속 검증
    private PayPolicy getStorePolicy(Long payPolicyId, Store store) {
        PayPolicy policy = payPolicyRepository.findById(payPolicyId)
                .orElseThrow(PayPolicyNotFoundException::new);

        if (!policy.getStore().getId().equals(store.getId())) {
            throw new StoreAccessDeniedException();
        }

        return policy;
    }



    // 전체 직원 조회
    @Transactional(readOnly = true)
    public List<EmployeeInfoResponse> getAllEmployees(Long employerId, Long storeId) {

        // 조회 가능한 가게인지 여부
        Store store = storeAccessService.getMyStore(storeId, employerId);

        List<Employee> employees = employeeRepository.findByStoreId(storeId);
        List<EmployeeInfoResponse> responses = employeeMapper.toInfoResponseList(employees);

        return responses;
    }

    // 직원 정보 조회
    @Transactional(readOnly = true)
    public EmployeeInfoResponse getEmployee(
            Long employerId,
            Long storeId,
            Long employeeId
    ) {
        // 사장 소유 가게 검증 + 조회
        Store store = storeAccessService.getMyStore(storeId, employerId);

        // 해당 가게 소속 직원 검증 + 조회
        Employee employee = employeeAccessService.getEmployee(employeeId, store);

        EmployeeInfoResponse response = employeeMapper.toInfoResponse(employee);
        return response;
    }

}
