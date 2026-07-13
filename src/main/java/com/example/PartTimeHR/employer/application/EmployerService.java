package com.example.PartTimeHR.employer.application;

import com.example.PartTimeHR.auth.domain.RefreshTokenRepository;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.domain.EmployerNotFoundException;
import com.example.PartTimeHR.employer.domain.EmployerRepository;
import com.example.PartTimeHR.employer.presentation.dto.EmployerInfoResponse;
import com.example.PartTimeHR.employer.presentation.dto.UpdateEmployerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployerService {
    private final EmployerRepository employerRepository;
    private final EmployerMapper employerMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    // 본인 조회(Employer)
    @Transactional(readOnly = true)
    public EmployerInfoResponse getMyInfo(Long employerId) {

        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(EmployerNotFoundException::new);

        return employerMapper.toInfoResponse(employer);
    }

    // Update
    @Transactional
    public EmployerInfoResponse updateInfo(Long employerId, UpdateEmployerRequest request){

        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(EmployerNotFoundException::new);

        if (request.getPassword() != null) {
            if (!request.getPassword().equals(request.getPasswordConfirm())) {
                throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
            }
            // 반드시 암호화 후 저장
            employer.getAccount().changePassword(passwordEncoder.encode(request.getPassword()));

            // 비밀번호가 바뀌면 기존 세션(refresh 토큰) 폐기
            refreshTokenRepository.deleteByAccountId(employer.getId());
        }

        employer.updateBasicInfo(request.getName(), request.getPhone());

        // dirty checking으로 자동 저장
        return employerMapper.toInfoResponse(employer);
    }
}
