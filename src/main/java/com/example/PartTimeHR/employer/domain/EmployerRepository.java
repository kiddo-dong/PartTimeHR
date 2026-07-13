package com.example.PartTimeHR.employer.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {

    // 이메일은 Account 소유지만, 연관 경로를 통해 조회할 수 있다
    Optional<Employer> findByAccount_Email(String email);
}
